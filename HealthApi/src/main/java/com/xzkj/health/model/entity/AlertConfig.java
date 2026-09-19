package com.xzkj.health.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("alert_config")
public class AlertConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String configName;
    private Integer configType;
    private String unit;
    private BigDecimal normalMin;
    private BigDecimal normalMax;
    private BigDecimal warnLow;
    private BigDecimal warnHigh;
    private BigDecimal warnMidLow;
    private BigDecimal warnMidHigh;
    private BigDecimal criticalLow;
    private BigDecimal criticalHigh;
    private Integer enabled;
    private Integer riskLevel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public Integer getConfigType() {
        return configType;
    }

    public void setConfigType(Integer configType) {
        this.configType = configType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getNormalMin() {
        return normalMin;
    }

    public void setNormalMin(BigDecimal normalMin) {
        this.normalMin = normalMin;
    }

    public BigDecimal getNormalMax() {
        return normalMax;
    }

    public void setNormalMax(BigDecimal normalMax) {
        this.normalMax = normalMax;
    }

    public BigDecimal getWarnLow() {
        return warnLow;
    }

    public void setWarnLow(BigDecimal warnLow) {
        this.warnLow = warnLow;
    }

    public BigDecimal getWarnHigh() {
        return warnHigh;
    }

    public void setWarnHigh(BigDecimal warnHigh) {
        this.warnHigh = warnHigh;
    }

    public BigDecimal getWarnMidLow() {
        return warnMidLow;
    }

    public void setWarnMidLow(BigDecimal warnMidLow) {
        this.warnMidLow = warnMidLow;
    }

    public BigDecimal getWarnMidHigh() {
        return warnMidHigh;
    }

    public void setWarnMidHigh(BigDecimal warnMidHigh) {
        this.warnMidHigh = warnMidHigh;
    }

    public BigDecimal getCriticalLow() {
        return criticalLow;
    }

    public void setCriticalLow(BigDecimal criticalLow) {
        this.criticalLow = criticalLow;
    }

    public BigDecimal getCriticalHigh() {
        return criticalHigh;
    }

    public void setCriticalHigh(BigDecimal criticalHigh) {
        this.criticalHigh = criticalHigh;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
