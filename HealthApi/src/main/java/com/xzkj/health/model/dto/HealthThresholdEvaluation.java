package com.xzkj.health.model.dto;

import java.math.BigDecimal;

public class HealthThresholdEvaluation {
    private final String empCode;
    private final Integer configType;
    private final Long configId;
    private final String configName;
    private final String unit;
    private final BigDecimal value;
    private final String severity;
    private final String direction;
    private final boolean outOfRange;
    private final Integer employeeRiskLevel;
    private final boolean defaultFallback;

    public HealthThresholdEvaluation(String empCode, Integer configType, Long configId,
                                     String configName, String unit, BigDecimal value,
                                     String severity, String direction, boolean outOfRange,
                                     Integer employeeRiskLevel, boolean defaultFallback) {
        this.empCode = empCode;
        this.configType = configType;
        this.configId = configId;
        this.configName = configName;
        this.unit = unit;
        this.value = value;
        this.severity = severity;
        this.direction = direction;
        this.outOfRange = outOfRange;
        this.employeeRiskLevel = employeeRiskLevel;
        this.defaultFallback = defaultFallback;
    }

    public String getEmpCode() { return empCode; }
    public Integer getConfigType() { return configType; }
    public Long getConfigId() { return configId; }
    public String getConfigName() { return configName; }
    public String getUnit() { return unit; }
    public BigDecimal getValue() { return value; }
    public String getSeverity() { return severity; }
    public String getDirection() { return direction; }
    public boolean isOutOfRange() { return outOfRange; }
    public Integer getEmployeeRiskLevel() { return employeeRiskLevel; }
    public boolean isDefaultFallback() { return defaultFallback; }
}
