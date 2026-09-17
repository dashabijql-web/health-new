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

    @Test
    void updatesDepartmentWhenIdExists() {
        Department existing = new Department();
        existing.setId(21L);
        existing.setDeptName("学习测试部门");
        existing.setDeptCode("LEARN01");
        existing.setStatus(0);
        existing.setSortOrder(0);
        when(departmentMapper.selectById(21L)).thenReturn(existing);
        when(departmentMapper.updateById(existing)).thenReturn(1);

        Department patch = new Department();
        patch.setId(21L);
        patch.setDeptName(" 学习测试部门-改 ");
        patch.setDeptCode(" LEARN01-U ");

        Department updated = departmentService.update(patch);

        assertEquals(21L, updated.getId());
        assertEquals("学习测试部门-改", updated.getDeptName());
        assertEquals("LEARN01-U", updated.getDeptCode());
        assertEquals(0, updated.getStatus());
        verify(departmentMapper).updateById(existing);
    }

    @Test
    void rejectsUpdateWhenDepartmentDoesNotExist() {
        when(departmentMapper.selectById(99L)).thenReturn(null);

        Department patch = new Department();
        patch.setId(99L);
        patch.setDeptName("学习测试部门");
        patch.setDeptCode("LEARN01");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.update(patch));

        assertEquals("部门不存在", exception.getMessage());
        verify(departmentMapper, never()).updateById(any(Department.class));
    }

    @Test
    void rejectsUpdateWhenNameIsBlank() {
        Department existing = new Department();
        existing.setId(21L);
        existing.setDeptName("学习测试部门");
        existing.setDeptCode("LEARN01");
        when(departmentMapper.selectById(21L)).thenReturn(existing);

        Department patch = new Department();
        patch.setId(21L);
        patch.setDeptName("  ");
        patch.setDeptCode("LEARN01");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.update(patch));

        assertEquals("部门名称不能为空", exception.getMessage());
        verify(departmentMapper, never()).updateById(any(Department.class));
    }

    @Test
    void deletesDepartmentWhenIdExists() {
        Department existing = new Department();
        existing.setId(21L);
        existing.setDeptName("学习测试部门");
        when(departmentMapper.selectById(21L)).thenReturn(existing);
        when(departmentMapper.deleteById(21L)).thenReturn(1);

        departmentService.delete(21L);

        verify(departmentMapper).deleteById(21L);
    }

    @Test
    void rejectsDeleteWhenDepartmentDoesNotExist() {
        when(departmentMapper.selectById(99L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.delete(99L));

        assertEquals("部门不存在", exception.getMessage());
        verify(departmentMapper, never()).deleteById(99L);
    }
}
