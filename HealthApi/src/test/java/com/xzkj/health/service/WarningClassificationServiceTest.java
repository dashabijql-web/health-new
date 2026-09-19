package com.xzkj.health.service;

import com.xzkj.health.model.dto.WarningSourceCatalog;
import com.xzkj.health.model.entity.WarningRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarningClassificationServiceTest {
    private final WarningClassificationService service = new WarningClassificationService();

    @Test
    void catalogsThreeIndependentWarningSources() {
        List<WarningSourceCatalog> catalog = service.catalog();

        assertEquals(List.of("HEALTH_THRESHOLD", "DEVICE_ALARM", "TREND_WARNING"),
                catalog.stream().map(WarningSourceCatalog::getSource).toList());
        WarningSourceCatalog device = catalog.get(1);
        assertTrue(device.getCodes().stream().anyMatch(code -> "SOS".equals(code.getCode())));
        assertTrue(catalog.get(0).getCodes().stream().noneMatch(code -> "SOS".equals(code.getCode())));
        assertTrue(catalog.get(2).getCodes().stream().noneMatch(code -> "SOS".equals(code.getCode())));
    }

    @Test
    void acceptsHealthThresholdWithSnapshot() {
        WarningRecord warning = warning("HEALTH_THRESHOLD", "HEART_RATE");
        warning.setThresholdSnapshot("{\"configId\":21}");

        service.validate(warning);
    }

    @Test
    void rejectsSosOutsideDeviceAlarmSource() {
        WarningRecord warning = warning("HEALTH_THRESHOLD", "SOS");
        warning.setThresholdSnapshot("{}");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.validate(warning));

        assertEquals("事件代码不属于指定的预警来源", exception.getMessage());
    }

    @Test
    void requiresImeiForDeviceAlarm() {
        WarningRecord warning = warning("DEVICE_ALARM", "FALL");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.validate(warning));

        assertEquals("设备报警必须包含设备IMEI", exception.getMessage());
    }

    @Test
    void acceptsTrendWarningWithoutDeviceOrThresholdContext() {
        service.validate(warning("TREND_WARNING", "HEART_RATE_TREND"));
    }

    private static WarningRecord warning(String source, String code) {
        WarningRecord warning = new WarningRecord();
        warning.setEventSource(source);
        warning.setEventCode(code);
        return warning;
    }
}
