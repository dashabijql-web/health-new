package com.xzkj.health.controller;

import java.math.BigDecimal;

public class HealthThresholdEvaluateRequest {
    private String empCode;
    private Integer configType;
    private BigDecimal value;

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public Integer getConfigType() {
        return configType;
    }

    public void setConfigType(Integer configType) {
        this.configType = configType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
