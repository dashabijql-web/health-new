package com.xzkj.health.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xzkj.health.mapper.WarningRecordMapper;
import com.xzkj.health.model.dto.EffectiveAlertConfig;
import com.xzkj.health.model.dto.HealthThresholdEvaluation;
import com.xzkj.health.model.dto.WarningGenerationResult;
import com.xzkj.health.model.entity.AlertConfig;
import com.xzkj.health.model.entity.WarningRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthWarningServiceTest {
    @Mock
    HealthThresholdService healthThresholdService;
    @Mock
    AlertConfigService alertConfigService;
    @Mock
    WarningRecordMapper warningRecordMapper;
    @Mock
    WarningClassificationService warningClassificationService;

    HealthWarningService healthWarningService;

    @BeforeEach
    void setUp() {
        healthWarningService = new HealthWarningService(
                healthThresholdService, alertConfigService, warningRecordMapper,
                warningClassificationService, new ObjectMapper());
    }

    @Test
    void doesNotInsertWarningForNormalValue() {
        EffectiveAlertConfig effective = effectiveConfig();
        HealthThresholdEvaluation evaluation = evaluation("NORMAL", "NONE", false);
        when(alertConfigService.effective("EMP0001", 1)).thenReturn(effective);
        when(healthThresholdService.evaluate(effective, 1, new BigDecimal("80"))).thenReturn(evaluation);

        WarningGenerationResult result = healthWarningService.evaluateAndCreate(
                "EMP0001", 1, new BigDecimal("80"));

        assertFalse(result.isCreated());
        assertNull(result.getWarning());
        verify(warningRecordMapper, never()).insertToTable(anyString(), any());
    }

    @Test
    void insertsStructuredHealthThresholdWarning() {
        EffectiveAlertConfig effective = effectiveConfig();
        HealthThresholdEvaluation evaluation = evaluation("HIGH", "HIGH", true);
        when(alertConfigService.effective("EMP0001", 1)).thenReturn(effective);
        when(healthThresholdService.evaluate(effective, 1, new BigDecimal("151"))).thenReturn(evaluation);
        when(warningRecordMapper.insertToTable(anyString(), any(WarningRecord.class))).thenAnswer(invocation -> {
            WarningRecord warning = invocation.getArgument(1);
            warning.setId(88L);
            return 1;
        });

        WarningGenerationResult result = healthWarningService.evaluateAndCreate(
                "EMP0001", 1, new BigDecimal("151"));

        assertTrue(result.isCreated());
        assertEquals(88L, result.getWarning().getId());
        assertEquals("心率偏高", result.getWarning().getWarningType());
        assertEquals("心率", result.getWarning().getIndicatorName());
        assertEquals("151 bpm", result.getWarning().getIndicatorValue());
        assertEquals("高危", result.getWarning().getWarningLevel());
        assertEquals("HEALTH_THRESHOLD", result.getWarning().getEventSource());
        assertEquals("HEART_RATE", result.getWarning().getEventCode());
        assertFalse(result.getWarning().getHandled());
        assertTrue(result.getWarning().getThresholdSnapshot().contains("\"configId\":21"));
        assertTrue(result.getWarning().getThresholdSnapshot().contains("\"employeeRiskLevel\":3"));
        verify(warningClassificationService).validate(result.getWarning());

        ArgumentCaptor<String> tableCaptor = ArgumentCaptor.forClass(String.class);
        verify(warningRecordMapper).insertToTable(tableCaptor.capture(), any(WarningRecord.class));
        assertEquals("warning_record_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")),
                tableCaptor.getValue());
    }

    @Test
    void rejectsWhenInsertDoesNotAffectExactlyOneRow() {
        EffectiveAlertConfig effective = effectiveConfig();
        HealthThresholdEvaluation evaluation = evaluation("LOW", "LOW", true);
        when(alertConfigService.effective("EMP0001", 1)).thenReturn(effective);
        when(healthThresholdService.evaluate(effective, 1, new BigDecimal("49"))).thenReturn(evaluation);
        when(warningRecordMapper.insertToTable(anyString(), any(WarningRecord.class))).thenReturn(0);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> healthWarningService.evaluateAndCreate("EMP0001", 1, new BigDecimal("49")));

        assertEquals("预警记录写入失败", exception.getMessage());
    }

    private static EffectiveAlertConfig effectiveConfig() {
        AlertConfig config = new AlertConfig();
        config.setId(21L);
        config.setConfigType(1);
        config.setConfigName("心率");
        config.setUnit("bpm");
        config.setRiskLevel(3);
        config.setWarnLow(new BigDecimal("45"));
        config.setWarnHigh(new BigDecimal("130"));
        config.setWarnMidLow(new BigDecimal("40"));
        config.setWarnMidHigh(new BigDecimal("145"));
        config.setCriticalLow(new BigDecimal("35"));
        config.setCriticalHigh(new BigDecimal("160"));
        return new EffectiveAlertConfig("EMP0001", 3, false, config);
    }

    private static HealthThresholdEvaluation evaluation(String severity, String direction,
                                                        boolean outOfRange) {
        BigDecimal value = outOfRange ? new BigDecimal("151") : new BigDecimal("80");
        return new HealthThresholdEvaluation(
                "EMP0001", 1, 21L, "心率", "bpm", value,
                severity, direction, outOfRange, 3, false);
    }
}
