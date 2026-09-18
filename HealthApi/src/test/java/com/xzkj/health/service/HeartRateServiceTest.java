package com.xzkj.health.service;

import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.HeartRateMapper;
import com.xzkj.health.model.dto.HeartRatePage;
import com.xzkj.health.model.dto.HeartRateRecord;
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
class HeartRateServiceTest {
    @Mock
    HeartRateMapper heartRateMapper;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    HeartRateService heartRateService;

    @Test
    void listsHeartRateForEmployee() {
        HeartRateRecord record = new HeartRateRecord();
        record.setHeartRate(78);
        when(heartRateMapper.countList("EMP0001", null, null)).thenReturn(1L);
        when(heartRateMapper.findPage("EMP0001", null, null, 0, 20)).thenReturn(List.of(record));

        HeartRatePage page = heartRateService.list(" EMP0001 ", null, null, null, null);

        assertEquals(1L, page.getTotal());
        assertEquals(78, page.getList().get(0).getHeartRate());
    }

    @Test
    void rejectsBlankEmpCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> heartRateService.list("  ", null, null, 1, 20));

        assertEquals("工号不能为空", exception.getMessage());
        verify(heartRateMapper, never()).findPage(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void rejectsInvalidHeartRate() {
        Employee employee = new Employee();
        employee.setEmpCode("LEARN-EMP");
        employee.setEmpName("学习测试人员");
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> heartRateService.create("LEARN-EMP", 10, null));

        assertEquals("心率必须在20到300之间", exception.getMessage());
        verify(heartRateMapper, never()).insertRecord(anyString(), anyString(), any(), any());
    }

    @Test
    void createsHeartRateForExistingEmployee() {
        Employee employee = new Employee();
        employee.setEmpCode("LEARN-EMP");
        employee.setEmpName("学习测试人员");
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee);
        when(heartRateMapper.insertRecord(anyString(), eq("LEARN-EMP"), eq(76), anyString())).thenReturn(1);

        HeartRateRecord created = heartRateService.create("LEARN-EMP", 76, null);

        assertEquals(76, created.getHeartRate());
        assertEquals("LEARN-EMP", created.getEmpCode());
        verify(heartRateMapper).insertRecord(anyString(), eq("LEARN-EMP"), eq(76), anyString());
    }
}
