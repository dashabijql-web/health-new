package com.xzkj.health.service;

import com.xzkj.health.mapper.DepartmentMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.JobTypeMapper;
import com.xzkj.health.model.dto.EmployeeListItem;
import com.xzkj.health.model.dto.EmployeePage;
import com.xzkj.health.model.entity.Department;
import com.xzkj.health.model.entity.Employee;
import com.xzkj.health.model.entity.JobType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    EmployeeMapper employeeMapper;

    @Mock
    DepartmentMapper departmentMapper;

    @Mock
    JobTypeMapper jobTypeMapper;

    @InjectMocks
    EmployeeService employeeService;

    @Test
    void usesDefaultPageAndSizeWhenMissing() {
        when(employeeMapper.countList(null)).thenReturn(1000L);
        when(employeeMapper.findPage(null, 0, 20)).thenReturn(List.of(new EmployeeListItem()));

        EmployeePage page = employeeService.list("  ", null, null);

        assertEquals(1000L, page.getTotal());
        assertEquals(1, page.getPage());
        assertEquals(20, page.getSize());
        verify(employeeMapper).findPage(null, 0, 20);
    }

    @Test
    void capsPageSize() {
        when(employeeMapper.countList("张")).thenReturn(3L);
        when(employeeMapper.findPage("张", 0, 50)).thenReturn(List.of());

        EmployeePage page = employeeService.list("张", 1, 200);

        assertEquals(50, page.getSize());
        verify(employeeMapper).findPage("张", 0, 50);
    }

    @Test
    void createsEmployeeWhenNameAndCodeArePresent() {
        Employee employee = new Employee();
        employee.setEmpName(" 学习测试人员 ");
        employee.setEmpCode(" LEARN-EMP ");
        employee.setDeptId(10L);
        employee.setJobTypeId(1L);
        when(employeeMapper.countByEmpCode("LEARN-EMP", null)).thenReturn(0L);
        when(departmentMapper.selectById(10L)).thenReturn(new Department());
        when(jobTypeMapper.selectById(1L)).thenReturn(new JobType());
        when(employeeMapper.insert(any(Employee.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Employee.class).setId(2000L);
            return 1;
        });

        Employee created = employeeService.create(employee);

        assertEquals(2000L, created.getId());
        assertEquals("学习测试人员", created.getEmpName());
        assertEquals("LEARN-EMP", created.getEmpCode());
        assertEquals(1, created.getGender());
        assertEquals(0, created.getStatus());
        verify(employeeMapper).insert(employee);
    }

    @Test
    void rejectsDuplicateEmpCode() {
        Employee employee = new Employee();
        employee.setEmpName("学习测试人员");
        employee.setEmpCode("EMP0001");
        when(employeeMapper.countByEmpCode("EMP0001", null)).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.create(employee));

        assertEquals("工号已存在", exception.getMessage());
        verify(employeeMapper, never()).insert(any(Employee.class));
    }

    @Test
    void rejectsUnknownDepartment() {
        Employee employee = new Employee();
        employee.setEmpName("学习测试人员");
        employee.setEmpCode("LEARN-EMP");
        employee.setDeptId(999L);
        when(employeeMapper.countByEmpCode("LEARN-EMP", null)).thenReturn(0L);
        when(departmentMapper.selectById(999L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.create(employee));

        assertEquals("部门不存在", exception.getMessage());
        verify(employeeMapper, never()).insert(any(Employee.class));
    }

    @Test
    void deletesEmployeeWhenIdExists() {
        Employee existing = new Employee();
        existing.setId(2000L);
        when(employeeMapper.selectById(2000L)).thenReturn(existing);
        when(employeeMapper.deleteById(2000L)).thenReturn(1);

        employeeService.delete(2000L);

        verify(employeeMapper).deleteById(2000L);
    }
}
