package com.xzkj.health.service;

import com.xzkj.health.model.dto.EffectiveAlertConfig;
import com.xzkj.health.model.dto.HealthThresholdEvaluation;
import com.xzkj.health.model.entity.AlertConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class HealthThresholdService {
    private final AlertConfigService alertConfigService;

    public HealthThresholdService(AlertConfigService alertConfigService) {
        this.alertConfigService = alertConfigService;
    }

    public HealthThresholdEvaluation evaluate(String empCode, Integer configType, BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("指标值不能为空");
        }

        EffectiveAlertConfig effective = alertConfigService.effective(empCode, configType);
        AlertConfig config = effective.getConfig();
        requireCompleteThresholds(config);

        String severity = severity(value, config);
        String direction = direction(value, config);
        return new HealthThresholdEvaluation(
                effective.getEmpCode(), configType, config.getId(), config.getConfigName(),
                config.getUnit(), value, severity, direction, !"NORMAL".equals(severity),
                effective.getEmployeeRiskLevel(), effective.isDefaultFallback());
    }

    private String severity(BigDecimal value, AlertConfig config) {
        if (outside(value, config.getCriticalLow(), config.getCriticalHigh())) {
            return "HIGH";
        }
        if (outside(value, config.getWarnMidLow(), config.getWarnMidHigh())) {
            return "MEDIUM";
        }
        if (outside(value, config.getWarnLow(), config.getWarnHigh())) {
            return "LOW";
        }
        return "NORMAL";
    }

    private String direction(BigDecimal value, AlertConfig config) {
        if (value.compareTo(config.getWarnLow()) < 0) {
            return "LOW";
        }
        if (value.compareTo(config.getWarnHigh()) > 0) {
            return "HIGH";
        }
        return "NONE";
    }

    private boolean outside(BigDecimal value, BigDecimal low, BigDecimal high) {
        return value.compareTo(low) < 0 || value.compareTo(high) > 0;
    }

    private void requireCompleteThresholds(AlertConfig config) {
        if (config.getWarnLow() == null || config.getWarnHigh() == null
                || config.getWarnMidLow() == null || config.getWarnMidHigh() == null
                || config.getCriticalLow() == null || config.getCriticalHigh() == null) {
            throw new IllegalArgumentException("有效阈值配置不完整");
        }
    }
}
