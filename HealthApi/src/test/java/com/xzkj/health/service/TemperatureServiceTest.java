package com.xzkj.health.service;

import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.TemperatureMapper;
import com.xzkj.health.model.dto.TemperaturePage;
import com.xzkj.health.model.dto.TemperatureRecord;
import com.xzkj.health.model.entity.Employee;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemperatureServiceTest {
    @Mock
    TemperatureMapper temperatureMapper;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    TemperatureService temperatureService;

    @Test
    void listsTemperatureForEmployee() {
        TemperatureRecord record = new TemperatureRecord();
        record.setTemperature(new BigDecimal("36.7"));
        when(temperatureMapper.countList("EMP0001", null, null)).thenReturn(1L);
        when(temperatureMapper.findPage("EMP0001", null, null, 0, 20)).thenReturn(List.of(record));

        TemperaturePage page = temperatureService.list(" EMP0001 ", null, null, null, null);

        assertEquals(1L, page.getTotal());
        assertEquals(new BigDecimal("36.7"), page.getList().get(0).getTemperature());
    }

    @Test
    void rejectsInvalidTemperature() {
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee("LEARN-EMP"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> temperatureService.create("LEARN-EMP", new BigDecimal("34.9"), null));

        assertEquals("体温必须在35.0到42.0℃之间", exception.getMessage());
        verify(temperatureMapper, never()).insertRecord(anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void createsTemperatureUsingStorageScale() {
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee("LEARN-EMP"));
        when(temperatureMapper.insertRecord(anyString(), eq("LEARN-EMP"), eq(367), anyString())).thenReturn(1);

        TemperatureRecord created = temperatureService.create("LEARN-EMP", new BigDecimal("36.7"), null);

        assertEquals(new BigDecimal("36.7"), created.getTemperature());
        assertEquals("LEARN-EMP", created.getEmpCode());
        verify(temperatureMapper).insertRecord(anyString(), eq("LEARN-EMP"), eq(367), anyString());
    }

    private Employee employee(String empCode) {
        Employee employee = new Employee();
        employee.setEmpCode(empCode);
        employee.setEmpName("学习测试人员");
        return employee;
    }
}
