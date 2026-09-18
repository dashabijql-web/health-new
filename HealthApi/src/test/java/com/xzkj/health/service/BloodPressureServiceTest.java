package com.xzkj.health.service;

import com.xzkj.health.mapper.BloodPressureMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.model.dto.BloodPressurePage;
import com.xzkj.health.model.dto.BloodPressureRecord;
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
class BloodPressureServiceTest {
    @Mock
    BloodPressureMapper bloodPressureMapper;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    BloodPressureService bloodPressureService;

    @Test
    void listsBloodPressureForEmployee() {
        BloodPressureRecord record = new BloodPressureRecord();
        record.setSystolic(120);
        record.setDiastolic(80);
        when(bloodPressureMapper.countList("EMP0001", null, null)).thenReturn(1L);
        when(bloodPressureMapper.findPage("EMP0001", null, null, 0, 20)).thenReturn(List.of(record));

        BloodPressurePage page = bloodPressureService.list(" EMP0001 ", null, null, null, null);

        assertEquals(1L, page.getTotal());
        assertEquals(120, page.getList().get(0).getSystolic());
        assertEquals(80, page.getList().get(0).getDiastolic());
    }

    @Test
    void rejectsBlankEmpCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bloodPressureService.list("  ", null, null, 1, 20));

        assertEquals("工号不能为空", exception.getMessage());
        verify(bloodPressureMapper, never()).findPage(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void rejectsInvalidBloodPressure() {
        Employee employee = employee("LEARN-EMP");
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bloodPressureService.create("LEARN-EMP", 10, 80, null));

        assertEquals("收缩压必须在40到300之间", exception.getMessage());
        verify(bloodPressureMapper, never()).insertRecord(anyString(), anyString(), any(), any(), any());
    }

    @Test
    void rejectsWhenSystolicIsNotHigherThanDiastolic() {
        Employee employee = employee("LEARN-EMP");
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bloodPressureService.create("LEARN-EMP", 80, 120, null));

        assertEquals("收缩压必须高于舒张压", exception.getMessage());
        verify(bloodPressureMapper, never()).insertRecord(anyString(), anyString(), any(), any(), any());
    }

    @Test
    void createsBloodPressureForExistingEmployee() {
        Employee employee = employee("LEARN-EMP");
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee);
        when(bloodPressureMapper.insertRecord(
                anyString(), eq("LEARN-EMP"), eq(128), eq(82), anyString())).thenReturn(1);

        BloodPressureRecord created = bloodPressureService.create("LEARN-EMP", 128, 82, null);

        assertEquals(128, created.getSystolic());
        assertEquals(82, created.getDiastolic());
        assertEquals("LEARN-EMP", created.getEmpCode());
        verify(bloodPressureMapper).insertRecord(
                anyString(), eq("LEARN-EMP"), eq(128), eq(82), anyString());
    }

    private Employee employee(String empCode) {
        Employee employee = new Employee();
        employee.setEmpCode(empCode);
        employee.setEmpName("学习测试人员");
        return employee;
    }
}
