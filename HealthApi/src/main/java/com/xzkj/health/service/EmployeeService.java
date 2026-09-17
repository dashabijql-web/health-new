package com.xzkj.health.service;

import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.model.dto.EmployeePage;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;

    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    public EmployeePage list(String keyword, Integer page, Integer size) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        long total = employeeMapper.countList(normalized);
        return new EmployeePage(employeeMapper.findPage(normalized, offset, safeSize), total, safePage, safeSize);
    }
}
