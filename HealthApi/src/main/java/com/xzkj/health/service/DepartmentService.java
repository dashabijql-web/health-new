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

        applyNameAndCode(department, department.getDeptName(), department.getDeptCode());
        department.setId(null);
        if (department.getStatus() == null) {
            department.setStatus(0);
        }
        if (department.getSortOrder() == null) {
            department.setSortOrder(0);
        }
        departmentMapper.insert(department);
        return department;
    }

    public Department update(Department department) {
        if (department == null || department.getId() == null) {
            throw new IllegalArgumentException("部门ID不能为空");
        }
        Department existing = departmentMapper.selectById(department.getId());
        if (existing == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        applyNameAndCode(existing, department.getDeptName(), department.getDeptCode());
        departmentMapper.updateById(existing);
        return existing;
    }

    private void applyNameAndCode(Department target, String deptName, String deptCode) {
        String normalizedName = trimToEmpty(deptName);
        String normalizedCode = trimToEmpty(deptCode);
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
        if (normalizedCode.isEmpty()) {
            throw new IllegalArgumentException("部门编码不能为空");
        }
        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException("部门名称不能超过100个字符");
        }
        if (normalizedCode.length() > 50) {
            throw new IllegalArgumentException("部门编码不能超过50个字符");
        }
        target.setDeptName(normalizedName);
        target.setDeptCode(normalizedCode);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
