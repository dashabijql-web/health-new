package com.xzkj.health.service;

import com.xzkj.health.mapper.DepartmentMapper;
import com.xzkj.health.model.entity.Department;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {
    @Mock
    DepartmentMapper departmentMapper;

    @InjectMocks
    DepartmentService departmentService;

    @Test
    void createsDepartmentWhenNameAndCodeArePresent() {
        Department department = new Department();
        department.setId(99L);
        department.setDeptName(" 学习测试部门 ");
        department.setDeptCode(" LEARN01 ");

        when(departmentMapper.insert(any(Department.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Department.class).setId(21L);
            return 1;
        });

        Department created = departmentService.create(department);

        assertEquals(21L, created.getId());
        assertEquals("学习测试部门", created.getDeptName());
        assertEquals("LEARN01", created.getDeptCode());
        assertEquals(0, created.getStatus());
        assertEquals(0, created.getSortOrder());
        verify(departmentMapper).insert(department);
    }

    @Test
    void rejectsBlankDepartmentName() {
        Department department = new Department();
        department.setDeptName("  ");
        department.setDeptCode("LEARN01");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.create(department));

        assertEquals("部门名称不能为空", exception.getMessage());
        verify(departmentMapper, never()).insert(any(Department.class));
    }

    @Test
    void rejectsBlankDepartmentCode() {
        Department department = new Department();
        department.setDeptName("学习测试部门");
        department.setDeptCode(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.create(department));

        assertEquals("部门编码不能为空", exception.getMessage());
        verify(departmentMapper, never()).insert(any(Department.class));
        assertNull(department.getId());
    }
}
