package com.xzkj.health.service;

import com.xzkj.health.mapper.DepartmentMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.JobTypeMapper;
import com.xzkj.health.model.dto.EmployeePage;
import com.xzkj.health.model.entity.Employee;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final JobTypeMapper jobTypeMapper;

    public EmployeeService(EmployeeMapper employeeMapper,
                           DepartmentMapper departmentMapper,
                           JobTypeMapper jobTypeMapper) {
        this.employeeMapper = employeeMapper;
        this.departmentMapper = departmentMapper;
        this.jobTypeMapper = jobTypeMapper;
    }

    public EmployeePage list(String keyword, Integer page, Integer size) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        long total = employeeMapper.countList(normalized);
        return new EmployeePage(employeeMapper.findPage(normalized, offset, safeSize), total, safePage, safeSize);
    }

    public Employee create(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("人员信息不能为空");
        }
        applyWritableFields(employee, employee, null);
        employee.setId(null);
        if (employee.getStatus() == null) {
            employee.setStatus(0);
        }
        employeeMapper.insert(employee);
        return employee;
    }

    public Employee update(Employee employee) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("人员ID不能为空");
        }
        Employee existing = employeeMapper.selectById(employee.getId());
        if (existing == null) {
            throw new IllegalArgumentException("人员不存在");
        }
        applyWritableFields(existing, employee, existing.getId());
        employeeMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("人员ID不能为空");
        }
        Employee existing = employeeMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("人员不存在");
        }
        employeeMapper.deleteById(id);
    }

    private void applyWritableFields(Employee target, Employee source, Long excludeId) {
        String empName = trimToEmpty(source.getEmpName());
        String empCode = trimToEmpty(source.getEmpCode());
        if (empName.isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (empCode.isEmpty()) {
            throw new IllegalArgumentException("工号不能为空");
        }
        if (empName.length() > 50) {
            throw new IllegalArgumentException("姓名不能超过50个字符");
        }
        if (empCode.length() > 50) {
            throw new IllegalArgumentException("工号不能超过50个字符");
        }
        if (employeeMapper.countByEmpCode(empCode, excludeId) > 0) {
            throw new IllegalArgumentException("工号已存在");
        }

        target.setEmpName(empName);
        target.setEmpCode(empCode);
        target.setGender(source.getGender() != null && source.getGender() == 2 ? 2 : 1);
        String phone = trimToEmpty(source.getPhone());
        target.setPhone(phone.isEmpty() ? null : phone);
        target.setDeptId(requireExistingDept(source.getDeptId()));
        target.setJobTypeId(requireExistingJobType(source.getJobTypeId()));
    }

    private Long requireExistingDept(Long deptId) {
        if (deptId == null || deptId <= 0) {
            return null;
        }
        if (departmentMapper.selectById(deptId) == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        return deptId;
    }

    private Long requireExistingJobType(Long jobTypeId) {
        if (jobTypeId == null || jobTypeId <= 0) {
            return null;
        }
        if (jobTypeMapper.selectById(jobTypeId) == null) {
            throw new IllegalArgumentException("岗位不存在");
        }
        return jobTypeId;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
