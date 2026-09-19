package com.xzkj.health.service;

import com.xzkj.health.mapper.AlertConfigMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.JobTypeMapper;
import com.xzkj.health.model.dto.EffectiveAlertConfig;
import com.xzkj.health.model.entity.AlertConfig;
import com.xzkj.health.model.entity.Employee;
import com.xzkj.health.model.entity.JobType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertConfigService {
    private final AlertConfigMapper alertConfigMapper;
    private final EmployeeMapper employeeMapper;
    private final JobTypeMapper jobTypeMapper;

    public AlertConfigService(AlertConfigMapper alertConfigMapper,
                              EmployeeMapper employeeMapper,
                              JobTypeMapper jobTypeMapper) {
        this.alertConfigMapper = alertConfigMapper;
        this.employeeMapper = employeeMapper;
        this.jobTypeMapper = jobTypeMapper;
    }

    public List<AlertConfig> list() {
        return alertConfigMapper.findList();
    }

    public EffectiveAlertConfig effective(String empCode, Integer configType) {
        String normalizedEmpCode = empCode == null ? "" : empCode.trim();
        if (normalizedEmpCode.isEmpty()) {
            throw new IllegalArgumentException("人员编码不能为空");
        }
        if (configType == null || configType <= 0) {
            throw new IllegalArgumentException("指标类型必须为正整数");
        }

        Employee employee = employeeMapper.findByEmpCode(normalizedEmpCode);
        if (employee == null) {
            throw new IllegalArgumentException("人员不存在");
        }

        Integer employeeRiskLevel = null;
        if (employee.getJobTypeId() != null) {
            JobType jobType = jobTypeMapper.selectById(employee.getJobTypeId());
            if (jobType != null) {
                employeeRiskLevel = jobType.getRiskLevel();
            }
        }

        AlertConfig config = alertConfigMapper.findEffective(configType, employeeRiskLevel);
        if (config == null) {
            throw new IllegalArgumentException("未找到已启用的有效阈值配置");
        }
        boolean defaultFallback = config.getRiskLevel() == null;
        return new EffectiveAlertConfig(normalizedEmpCode, employeeRiskLevel, defaultFallback, config);
    }

    public AlertConfig update(AlertConfig patch) {
        if (patch == null || patch.getId() == null) {
            throw new IllegalArgumentException("阈值配置ID不能为空");
        }

        AlertConfig existing = alertConfigMapper.selectById(patch.getId());
        if (existing == null) {
            throw new IllegalArgumentException("阈值配置不存在");
        }

        validateThresholds(patch);
        existing.setNormalMin(patch.getNormalMin());
        existing.setNormalMax(patch.getNormalMax());
        existing.setWarnLow(patch.getWarnLow());
        existing.setWarnHigh(patch.getWarnHigh());
        existing.setWarnMidLow(patch.getWarnMidLow());
        existing.setWarnMidHigh(patch.getWarnMidHigh());
        existing.setCriticalLow(patch.getCriticalLow());
        existing.setCriticalHigh(patch.getCriticalHigh());
        existing.setUpdateTime(LocalDateTime.now());
        alertConfigMapper.updateById(existing);
        return existing;
    }

    public AlertConfig toggle(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("阈值配置ID不能为空");
        }

        AlertConfig existing = alertConfigMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("阈值配置不存在");
        }

        existing.setEnabled(existing.getEnabled() == null || existing.getEnabled() == 0 ? 1 : 0);
        existing.setUpdateTime(LocalDateTime.now());
        alertConfigMapper.updateById(existing);
        return existing;
    }

    private void validateThresholds(AlertConfig config) {
        BigDecimal normalMin = requireValue("正常下限", config.getNormalMin());
        BigDecimal normalMax = requireValue("正常上限", config.getNormalMax());
        BigDecimal warnLow = requireValue("低侧预警阈值", config.getWarnLow());
        BigDecimal warnHigh = requireValue("高侧预警阈值", config.getWarnHigh());
        BigDecimal warnMidLow = requireValue("低侧中危阈值", config.getWarnMidLow());
        BigDecimal warnMidHigh = requireValue("高侧中危阈值", config.getWarnMidHigh());
        BigDecimal criticalLow = requireValue("低侧高危阈值", config.getCriticalLow());
        BigDecimal criticalHigh = requireValue("高侧高危阈值", config.getCriticalHigh());

        requireOrdered(normalMin, normalMax, "正常范围下限不能大于上限");
        requireOrdered(criticalLow, warnMidLow, "低侧高危阈值不能大于中危阈值");
        requireOrdered(warnMidLow, warnLow, "低侧中危阈值不能大于预警阈值");
        requireOrdered(warnLow, normalMin, "低侧预警阈值不能大于正常下限");
        requireOrdered(normalMax, warnHigh, "高侧预警阈值不能小于正常上限");
        requireOrdered(warnHigh, warnMidHigh, "高侧预警阈值不能大于中危阈值");
        requireOrdered(warnMidHigh, criticalHigh, "高侧中危阈值不能大于高危阈值");
    }

    private BigDecimal requireValue(String label, BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        return value;
    }

    private void requireOrdered(BigDecimal lower, BigDecimal upper, String message) {
        if (lower.compareTo(upper) > 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
