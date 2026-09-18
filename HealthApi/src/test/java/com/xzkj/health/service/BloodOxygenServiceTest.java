package com.xzkj.health.service;

import com.xzkj.health.mapper.BloodOxygenMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.model.dto.BloodOxygenPage;
import com.xzkj.health.model.dto.BloodOxygenRecord;
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
class BloodOxygenServiceTest {
    @Mock
    BloodOxygenMapper bloodOxygenMapper;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    BloodOxygenService bloodOxygenService;

    @Test
    void listsBloodOxygenForEmployee() {
        BloodOxygenRecord record = new BloodOxygenRecord();
        record.setBloodOxygen(98);
        when(bloodOxygenMapper.countList("EMP0001", null, null)).thenReturn(1L);
        when(bloodOxygenMapper.findPage("EMP0001", null, null, 0, 20)).thenReturn(List.of(record));

        BloodOxygenPage page = bloodOxygenService.list(" EMP0001 ", null, null, null, null);

        assertEquals(1L, page.getTotal());
        assertEquals(98, page.getList().get(0).getBloodOxygen());
    }

    @Test
    void rejectsBlankEmpCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bloodOxygenService.list("  ", null, null, 1, 20));

        assertEquals("工号不能为空", exception.getMessage());
        verify(bloodOxygenMapper, never()).findPage(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void rejectsInvalidBloodOxygen() {
        Employee employee = employee("LEARN-EMP");
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bloodOxygenService.create("LEARN-EMP", 49, null));

        assertEquals("血氧必须在50到100之间", exception.getMessage());
        verify(bloodOxygenMapper, never()).insertRecord(anyString(), anyString(), any(), any());
    }

    @Test
    void createsBloodOxygenForExistingEmployee() {
        Employee employee = employee("LEARN-EMP");
        when(employeeMapper.findByEmpCode("LEARN-EMP")).thenReturn(employee);
        when(bloodOxygenMapper.insertRecord(
                anyString(), eq("LEARN-EMP"), eq(98), anyString())).thenReturn(1);

        BloodOxygenRecord created = bloodOxygenService.create("LEARN-EMP", 98, null);

        assertEquals(98, created.getBloodOxygen());
        assertEquals("LEARN-EMP", created.getEmpCode());
        verify(bloodOxygenMapper).insertRecord(
                anyString(), eq("LEARN-EMP"), eq(98), anyString());
    }

    private Employee employee(String empCode) {
        Employee employee = new Employee();
        employee.setEmpCode(empCode);
        employee.setEmpName("学习测试人员");
        return employee;
    }
}
