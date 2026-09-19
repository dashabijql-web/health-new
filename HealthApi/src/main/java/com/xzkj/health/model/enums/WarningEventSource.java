package com.xzkj.health.model.enums;

public enum WarningEventSource {
    HEALTH_THRESHOLD("健康阈值"),
    DEVICE_ALARM("设备报警"),
    TREND_WARNING("趋势预警");

    private final String label;

    WarningEventSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
