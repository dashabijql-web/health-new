package com.xzkj.health.model.enums;

public enum WarningEventCode {
    HEART_RATE(WarningEventSource.HEALTH_THRESHOLD, "心率越界"),
    BLOOD_OXYGEN(WarningEventSource.HEALTH_THRESHOLD, "血氧越界"),
    TEMPERATURE(WarningEventSource.HEALTH_THRESHOLD, "体温越界"),
    SYSTOLIC_PRESSURE(WarningEventSource.HEALTH_THRESHOLD, "收缩压越界"),
    PRESSURE_INDEX(WarningEventSource.HEALTH_THRESHOLD, "压力指数越界"),

    SOS(WarningEventSource.DEVICE_ALARM, "主动求救"),
    FALL(WarningEventSource.DEVICE_ALARM, "跌倒报警"),
    AFIB(WarningEventSource.DEVICE_ALARM, "房颤报警"),
    TAMPER(WarningEventSource.DEVICE_ALARM, "拆卸报警"),
    INFRARED(WarningEventSource.DEVICE_ALARM, "红外报警"),
    DEVICE_UNKNOWN(WarningEventSource.DEVICE_ALARM, "其他设备报警"),

    HEART_RATE_TREND(WarningEventSource.TREND_WARNING, "心率趋势风险"),
    BLOOD_OXYGEN_TREND(WarningEventSource.TREND_WARNING, "血氧趋势风险"),
    TEMPERATURE_TREND(WarningEventSource.TREND_WARNING, "体温趋势风险"),
    SYSTOLIC_PRESSURE_TREND(WarningEventSource.TREND_WARNING, "收缩压趋势风险"),
    PRESSURE_INDEX_TREND(WarningEventSource.TREND_WARNING, "压力指数趋势风险");

    private final WarningEventSource source;
    private final String label;

    WarningEventCode(WarningEventSource source, String label) {
        this.source = source;
        this.label = label;
    }

    public WarningEventSource getSource() { return source; }
    public String getLabel() { return label; }
}
