package com.xzkj.health.service;

import com.xzkj.health.mapper.AlertConfigMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.JobTypeMapper;
import com.xzkj.health.model.dto.EffectiveAlertConfig;
import com.xzkj.health.model.entity.AlertConfig;
import com.xzkj.health.model.entity.Employee;
import com.xzkj.health.model.entity.JobType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertConfigServiceTest {
    @Mock
    AlertConfigMapper alertConfigMapper;

    @Mock
    EmployeeMapper employeeMapper;

    @Mock
    JobTypeMapper jobTypeMapper;

    @InjectMocks
    AlertConfigService alertConfigService;

    @Test
    void listsConfigsFromMapper() {
        List<AlertConfig> configs = List.of(validConfig());
        when(alertConfigMapper.findList()).thenReturn(configs);

        assertEquals(configs, alertConfigService.list());
        verify(alertConfigMapper).findList();
    }

    @Test
    void returnsRiskSpecificEffectiveConfig() {
        Employee employee = employee(8L);
        JobType jobType = new JobType();
        jobType.setRiskLevel(2);
        AlertConfig config = validConfig();
        config.setRiskLevel(2);
        when(employeeMapper.findByEmpCode("EMP0001")).thenReturn(employee);
        when(jobTypeMapper.selectById(8L)).thenReturn(jobType);
        when(alertConfigMapper.findEffective(1, 2)).thenReturn(config);

        EffectiveAlertConfig result = alertConfigService.effective(" EMP0001 ", 1);

        assertEquals("EMP0001", result.getEmpCode());
        assertEquals(2, result.getEmployeeRiskLevel());
        assertEquals(false, result.isDefaultFallback());
        assertEquals(config, result.getConfig());
    }

    @Test
    void fallsBackToDefaultConfigWhenRiskSpecificConfigIsUnavailable() {
        Employee employee = employee(8L);
        JobType jobType = new JobType();
        jobType.setRiskLevel(3);
        AlertConfig config = validConfig();
        config.setRiskLevel(null);
        when(employeeMapper.findByEmpCode("EMP0002")).thenReturn(employee);
        when(jobTypeMapper.selectById(8L)).thenReturn(jobType);
        when(alertConfigMapper.findEffective(4, 3)).thenReturn(config);

        EffectiveAlertConfig result = alertConfigService.effective("EMP0002", 4);

        assertEquals(3, result.getEmployeeRiskLevel());
        assertEquals(true, result.isDefaultFallback());
    }

    @Test
    void usesDefaultConfigWhenEmployeeHasNoJobType() {
        Employee employee = employee(null);
        AlertConfig config = validConfig();
        config.setRiskLevel(null);
        when(employeeMapper.findByEmpCode("EMP0003")).thenReturn(employee);
        when(alertConfigMapper.findEffective(2, null)).thenReturn(config);

        EffectiveAlertConfig result = alertConfigService.effective("EMP0003", 2);

        assertEquals(null, result.getEmployeeRiskLevel());
        assertEquals(true, result.isDefaultFallback());
        verify(jobTypeMapper, never()).selectById(any());
    }

    @Test
    void rejectsMissingEmployee() {
        when(employeeMapper.findByEmpCode("UNKNOWN")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> alertConfigService.effective("UNKNOWN", 1));

        assertEquals("人员不存在", exception.getMessage());
        verify(alertConfigMapper, never()).findEffective(any(), any());
    }

    @Test
    void rejectsWhenNoEnabledConfigCanBeResolved() {
        Employee employee = employee(null);
        when(employeeMapper.findByEmpCode("EMP0004")).thenReturn(employee);
        when(alertConfigMapper.findEffective(7, null)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> alertConfigService.effective("EMP0004", 7));

        assertEquals("未找到已启用的有效阈值配置", exception.getMessage());
    }

    @Test
    void updatesThresholdsButKeepsConfigIdentity() {
        AlertConfig existing = validConfig();
        existing.setId(3L);
        existing.setConfigName("心率");
        existing.setEnabled(1);
        when(alertConfigMapper.selectById(3L)).thenReturn(existing);
        when(alertConfigMapper.updateById(existing)).thenReturn(1);

        AlertConfig patch = validConfig();
        patch.setId(3L);
        patch.setNormalMin(new BigDecimal("61"));

        AlertConfig updated = alertConfigService.update(patch);

        assertEquals(new BigDecimal("61"), updated.getNormalMin());
        assertEquals("心率", updated.getConfigName());
        assertEquals(1, updated.getEnabled());
        verify(alertConfigMapper).updateById(existing);
    }

    @Test
    void rejectsThresholdsWithInvalidOrder() {
        AlertConfig existing = validConfig();
        existing.setId(3L);
        when(alertConfigMapper.selectById(3L)).thenReturn(existing);

        AlertConfig patch = validConfig();
        patch.setId(3L);
        patch.setWarnLow(new BigDecimal("61"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> alertConfigService.update(patch));

        assertEquals("低侧预警阈值不能大于正常下限", exception.getMessage());
        verify(alertConfigMapper, never()).updateById(any(AlertConfig.class));
    }

    @Test
    void rejectsUpdateWhenConfigDoesNotExist() {
        when(alertConfigMapper.selectById(99L)).thenReturn(null);

        AlertConfig patch = validConfig();
        patch.setId(99L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> alertConfigService.update(patch));

        assertEquals("阈值配置不存在", exception.getMessage());
        verify(alertConfigMapper, never()).updateById(any(AlertConfig.class));
    }

    @Test
    void togglesEnabledConfigOff() {
        AlertConfig existing = validConfig();
        existing.setId(3L);
        existing.setEnabled(1);
        when(alertConfigMapper.selectById(3L)).thenReturn(existing);
        when(alertConfigMapper.updateById(existing)).thenReturn(1);

        AlertConfig toggled = alertConfigService.toggle(3L);

        assertEquals(0, toggled.getEnabled());
        verify(alertConfigMapper).updateById(existing);
    }

    @Test
    void togglesNullEnabledConfigOn() {
        AlertConfig existing = validConfig();
        existing.setId(3L);
        existing.setEnabled(null);
        when(alertConfigMapper.selectById(3L)).thenReturn(existing);
        when(alertConfigMapper.updateById(existing)).thenReturn(1);

        assertEquals(1, alertConfigService.toggle(3L).getEnabled());
    }

    private static AlertConfig validConfig() {
        AlertConfig config = new AlertConfig();
        config.setConfigName("心率");
        config.setConfigType(1);
        config.setUnit("bpm");
        config.setNormalMin(new BigDecimal("60"));
        config.setNormalMax(new BigDecimal("100"));
        config.setWarnLow(new BigDecimal("50"));
        config.setWarnHigh(new BigDecimal("120"));
        config.setWarnMidLow(new BigDecimal("45"));
        config.setWarnMidHigh(new BigDecimal("135"));
        config.setCriticalLow(new BigDecimal("40"));
        config.setCriticalHigh(new BigDecimal("150"));
        config.setEnabled(1);
        return config;
    }

    private static Employee employee(Long jobTypeId) {
        Employee employee = new Employee();
        employee.setEmpCode("EMP0001");
        employee.setJobTypeId(jobTypeId);
        return employee;
    }
}
