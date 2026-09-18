package com.xzkj.health.service;

import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.SleepMapper;
import com.xzkj.health.model.dto.SleepPage;
import com.xzkj.health.model.dto.SleepRecord;
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
class SleepServiceTest {
    @Mock
    SleepMapper sleepMapper;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    SleepService sleepService;

    @Test
    void listsSleepForEmployee() {
        SleepRecord record = new SleepRecord();
        record.setSleepMinutes(480);
        when(sleepMapper.countList("EMP0001", null, null)).thenReturn(1L);
        when(sleepMapper.findPage("EMP0001", null, null, 0, 20)).thenReturn(List.of(record));

        SleepPage page = sleepService.list(" EMP0001 ", null, null, null, null);

        assertEquals(1L, page.getTotal());
        assertEquals(480, page.getList().get(0).getSleepMinutes());
    }

    @Test
    void rejectsInvalidSleepDuration() {
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee("LEARN-EMP"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sleepService.create("LEARN-EMP", 1440, null));

        assertEquals("睡眠时长必须在1到1439分钟之间", exception.getMessage());
        verify(sleepMapper, never()).insertRecord(anyString(), anyString(), any(), anyString());
    }

    @Test
    void createsSleepForExistingEmployee() {
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee("LEARN-EMP"));
        when(sleepMapper.insertRecord(anyString(), eq("LEARN-EMP"), eq(480), anyString())).thenReturn(1);

        SleepRecord created = sleepService.create("LEARN-EMP", 480, null);

        assertEquals(480, created.getSleepMinutes());
        assertEquals("LEARN-EMP", created.getEmpCode());
        verify(sleepMapper).insertRecord(anyString(), eq("LEARN-EMP"), eq(480), anyString());
    }

    private Employee employee(String empCode) {
        Employee employee = new Employee();
        employee.setEmpCode(empCode);
        employee.setEmpName("学习测试人员");
        return employee;
    }
}
