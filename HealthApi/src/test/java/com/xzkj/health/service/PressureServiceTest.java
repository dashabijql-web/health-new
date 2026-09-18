package com.xzkj.health.service;

import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.PressureMapper;
import com.xzkj.health.model.dto.PressurePage;
import com.xzkj.health.model.dto.PressureRecord;
import com.xzkj.health.model.entity.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class PressureServiceTest {
    @Mock
    PressureMapper pressureMapper;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    PressureService pressureService;

    @Test
    void listsPressureForEmployee() {
        PressureRecord record = new PressureRecord();
        record.setPressure(65);
        when(pressureMapper.countList("EMP0001", null, null)).thenReturn(1L);
        when(pressureMapper.findPage("EMP0001", null, null, 0, 20)).thenReturn(List.of(record));

        PressurePage page = pressureService.list(" EMP0001 ", null, null, null, null);

        assertEquals(1L, page.getTotal());
        assertEquals(65, page.getList().get(0).getPressure());
    }

    @Test
    void rejectsInvalidPressure() {
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee("LEARN-EMP"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pressureService.create("LEARN-EMP", 29, null));

        assertEquals("压力指数必须在30到100之间", exception.getMessage());
        verify(pressureMapper, never()).insertRecord(anyString(), anyString(), any(), anyString());
    }

    @Test
    void createsPressureForExistingEmployee() {
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee("LEARN-EMP"));
        when(pressureMapper.insertRecord(anyString(), eq("LEARN-EMP"), eq(65), anyString())).thenReturn(1);

        PressureRecord created = pressureService.create("LEARN-EMP", 65, null);

        assertEquals(65, created.getPressure());
        assertEquals("LEARN-EMP", created.getEmpCode());
        verify(pressureMapper).insertRecord(anyString(), eq("LEARN-EMP"), eq(65), anyString());
    }

    private Employee employee(String empCode) {
        Employee employee = new Employee();
        employee.setEmpCode(empCode);
        employee.setEmpName("学习测试人员");
        return employee;
    }
}
