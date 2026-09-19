package com.xzkj.health.service;

import com.xzkj.health.model.dto.EffectiveAlertConfig;
import com.xzkj.health.model.dto.HealthThresholdEvaluation;
import com.xzkj.health.model.entity.AlertConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthThresholdServiceTest {
    @Mock
    AlertConfigService alertConfigService;

    HealthThresholdService healthThresholdService;

    @BeforeEach
    void setUp() {
        healthThresholdService = new HealthThresholdService(alertConfigService);
    }

    @Test
    void classifiesNormalValueAndBoundaryValuesAsNormal() {
        stubEffective(validConfig());

        assertEvaluation("60", "NORMAL", "NONE", false);
        assertEvaluation("50", "NORMAL", "NONE", false);
        assertEvaluation("120", "NORMAL", "NONE", false);
    }

    @Test
    void classifiesLowSideSeverity() {
        stubEffective(validConfig());

        assertEvaluation("49", "LOW", "LOW", true);
        assertEvaluation("44", "MEDIUM", "LOW", true);
        assertEvaluation("39", "HIGH", "LOW", true);
    }

    @Test
    void classifiesHighSideSeverity() {
        stubEffective(validConfig());

        assertEvaluation("121", "LOW", "HIGH", true);
        assertEvaluation("136", "MEDIUM", "HIGH", true);
        assertEvaluation("151", "HIGH", "HIGH", true);
    }

    @Test
    void returnsConfigAndResolutionContext() {
        AlertConfig config = validConfig();
        stubEffective(config);

        HealthThresholdEvaluation result = healthThresholdService.evaluate(
                "EMP0001", 1, new BigDecimal("151"));

        assertEquals("EMP0001", result.getEmpCode());
        assertEquals(1, result.getConfigType());
        assertEquals(3L, result.getConfigId());
        assertEquals("心率", result.getConfigName());
        assertEquals("bpm", result.getUnit());
        assertEquals(2, result.getEmployeeRiskLevel());
        assertFalse(result.isDefaultFallback());
    }

    @Test
    void rejectsNullValueBeforeResolvingConfig() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> healthThresholdService.evaluate("EMP0001", 1, null));

        assertEquals("指标值不能为空", exception.getMessage());
    }

    @Test
    void rejectsIncompleteEffectiveConfig() {
        AlertConfig config = validConfig();
        config.setCriticalHigh(null);
        stubEffective(config);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> healthThresholdService.evaluate("EMP0001", 1, new BigDecimal("80")));

        assertEquals("有效阈值配置不完整", exception.getMessage());
    }

    private void assertEvaluation(String value, String severity, String direction, boolean outOfRange) {
        HealthThresholdEvaluation result = healthThresholdService.evaluate(
                "EMP0001", 1, new BigDecimal(value));
        assertEquals(severity, result.getSeverity());
        assertEquals(direction, result.getDirection());
        if (outOfRange) {
            assertTrue(result.isOutOfRange());
        } else {
            assertFalse(result.isOutOfRange());
        }
    }

    private void stubEffective(AlertConfig config) {
        when(alertConfigService.effective("EMP0001", 1))
                .thenReturn(new EffectiveAlertConfig("EMP0001", 2, false, config));
    }

    private static AlertConfig validConfig() {
        AlertConfig config = new AlertConfig();
        config.setId(3L);
        config.setConfigName("心率");
        config.setConfigType(1);
        config.setUnit("bpm");
        config.setWarnLow(new BigDecimal("50"));
        config.setWarnHigh(new BigDecimal("120"));
        config.setWarnMidLow(new BigDecimal("45"));
        config.setWarnMidHigh(new BigDecimal("135"));
        config.setCriticalLow(new BigDecimal("40"));
        config.setCriticalHigh(new BigDecimal("150"));
        return config;
    }
}
