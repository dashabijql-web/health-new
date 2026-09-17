package com.xzkj.health.service;

import com.xzkj.health.mapper.DepartmentMapper;
import com.xzkj.health.model.entity.Department;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public List<Department> list(String keyword) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return departmentMapper.findList(normalized);
    }

    public Department create(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("部门信息不能为空");
        }

        String deptName = trimToEmpty(department.getDeptName());
        String deptCode = trimToEmpty(department.getDeptCode());
        if (deptName.isEmpty()) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
        if (deptCode.isEmpty()) {
            throw new IllegalArgumentException("部门编码不能为空");
        }
        if (deptName.length() > 100) {
            throw new IllegalArgumentException("部门名称不能超过100个字符");
        }
        if (deptCode.length() > 50) {
            throw new IllegalArgumentException("部门编码不能超过50个字符");
        }

        department.setId(null);
        department.setDeptName(deptName);
        department.setDeptCode(deptCode);
        if (department.getStatus() == null) {
            department.setStatus(0);
        }
        if (department.getSortOrder() == null) {
            department.setSortOrder(0);
        }
        departmentMapper.insert(department);
        return department;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
