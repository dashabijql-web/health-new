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
}
