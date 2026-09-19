package com.xzkj.health.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xzkj.health.mapper.WarningRecordMapper;
import com.xzkj.health.model.dto.EffectiveAlertConfig;
import com.xzkj.health.model.dto.HealthThresholdEvaluation;
import com.xzkj.health.model.dto.WarningGenerationResult;
import com.xzkj.health.model.entity.AlertConfig;
import com.xzkj.health.model.entity.WarningRecord;
import com.xzkj.health.model.enums.WarningEventCode;
import com.xzkj.health.model.enums.WarningEventSource;
import com.xzkj.health.util.TableNameUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HealthWarningService {
    private final HealthThresholdService healthThresholdService;
    private final AlertConfigService alertConfigService;
    private final WarningRecordMapper warningRecordMapper;
    private final WarningClassificationService warningClassificationService;
    private final ObjectMapper objectMapper;

    public HealthWarningService(HealthThresholdService healthThresholdService,
                                AlertConfigService alertConfigService,
                                WarningRecordMapper warningRecordMapper,
                                WarningClassificationService warningClassificationService,
                                ObjectMapper objectMapper) {
        this.healthThresholdService = healthThresholdService;
        this.alertConfigService = alertConfigService;
        this.warningRecordMapper = warningRecordMapper;
        this.warningClassificationService = warningClassificationService;
        this.objectMapper = objectMapper;
    }

    public WarningGenerationResult evaluateAndCreate(String empCode, Integer configType,
                                                     BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("指标值不能为空");
        }
        EffectiveAlertConfig effective = alertConfigService.effective(empCode, configType);
        HealthThresholdEvaluation evaluation = healthThresholdService.evaluate(effective, configType, value);
        if (!evaluation.isOutOfRange()) {
            return new WarningGenerationResult(evaluation, false, null);
        }

        WarningRecord warning = buildWarning(evaluation, effective);
        warningClassificationService.validate(warning);
        String tableName = TableNameUtil.warningRecordTable(warning.getCreateTime());
        int rows = warningRecordMapper.insertToTable(tableName, warning);
        if (rows != 1) {
            throw new IllegalStateException("预警记录写入失败");
        }
        return new WarningGenerationResult(evaluation, true, warning);
    }

    private WarningRecord buildWarning(HealthThresholdEvaluation evaluation,
                                       EffectiveAlertConfig effective) {
        WarningRecord warning = new WarningRecord();
        warning.setUserCode(evaluation.getEmpCode());
        warning.setWarningType(evaluation.getConfigName() + directionLabel(evaluation.getDirection()));
        warning.setIndicatorName(evaluation.getConfigName());
        warning.setIndicatorValue(formatValue(evaluation.getValue(), evaluation.getUnit()));
        warning.setWarningLevel(severityLabel(evaluation.getSeverity()));
        warning.setEventSource(WarningEventSource.HEALTH_THRESHOLD.name());
        warning.setEventCode(eventCode(evaluation.getConfigType()));
        warning.setThresholdSnapshot(thresholdSnapshot(effective));
        warning.setHandled(false);
        warning.setCreateTime(LocalDateTime.now());
        return warning;
    }

    private String thresholdSnapshot(EffectiveAlertConfig effective) {
        AlertConfig config = effective.getConfig();
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("configId", config.getId());
        snapshot.put("configType", config.getConfigType());
        snapshot.put("configRiskLevel", config.getRiskLevel());
        snapshot.put("employeeRiskLevel", effective.getEmployeeRiskLevel());
        snapshot.put("defaultFallback", effective.isDefaultFallback());
        snapshot.put("warnLow", config.getWarnLow());
        snapshot.put("warnHigh", config.getWarnHigh());
        snapshot.put("warnMidLow", config.getWarnMidLow());
        snapshot.put("warnMidHigh", config.getWarnMidHigh());
        snapshot.put("criticalLow", config.getCriticalLow());
        snapshot.put("criticalHigh", config.getCriticalHigh());
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("阈值快照生成失败", exception);
        }
    }

    private String formatValue(BigDecimal value, String unit) {
        String number = value.stripTrailingZeros().toPlainString();
        return unit == null || unit.isBlank() ? number : number + " " + unit;
    }

    private String directionLabel(String direction) {
        return "LOW".equals(direction) ? "偏低" : "偏高";
    }

    private String severityLabel(String severity) {
        return switch (severity) {
            case "LOW" -> "低危";
            case "MEDIUM" -> "中危";
            case "HIGH" -> "高危";
            default -> throw new IllegalArgumentException("正常指标不能生成预警");
        };
    }

    private String eventCode(Integer configType) {
        return switch (configType) {
            case 1 -> WarningEventCode.HEART_RATE.name();
            case 2 -> WarningEventCode.BLOOD_OXYGEN.name();
            case 3 -> WarningEventCode.TEMPERATURE.name();
            case 4 -> WarningEventCode.SYSTOLIC_PRESSURE.name();
            case 5 -> WarningEventCode.PRESSURE_INDEX.name();
            default -> throw new IllegalArgumentException("指标类型没有对应的健康事件代码");
        };
    }
}
