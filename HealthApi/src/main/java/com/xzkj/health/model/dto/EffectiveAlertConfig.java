package com.xzkj.health.model.dto;

import com.xzkj.health.model.entity.AlertConfig;

public class EffectiveAlertConfig {
    private final String empCode;
    private final Integer employeeRiskLevel;
    private final boolean defaultFallback;
    private final AlertConfig config;

    public EffectiveAlertConfig(String empCode, Integer employeeRiskLevel,
                                boolean defaultFallback, AlertConfig config) {
        this.empCode = empCode;
        this.employeeRiskLevel = employeeRiskLevel;
        this.defaultFallback = defaultFallback;
        this.config = config;
    }

    public String getEmpCode() {
        return empCode;
    }

    public Integer getEmployeeRiskLevel() {
        return employeeRiskLevel;
    }

    public boolean isDefaultFallback() {
        return defaultFallback;
    }

    public AlertConfig getConfig() {
        return config;
    }
}
