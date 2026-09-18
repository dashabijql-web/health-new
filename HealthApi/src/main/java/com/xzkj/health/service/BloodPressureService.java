package com.xzkj.health.service;

import com.xzkj.health.mapper.BloodPressureMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.model.dto.BloodPressurePage;
import com.xzkj.health.model.dto.BloodPressureRecord;
import com.xzkj.health.model.entity.Employee;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BloodPressureService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;

    private final BloodPressureMapper bloodPressureMapper;
    private final EmployeeMapper employeeMapper;

    public BloodPressureService(BloodPressureMapper bloodPressureMapper, EmployeeMapper employeeMapper) {
        this.bloodPressureMapper = bloodPressureMapper;
        this.employeeMapper = employeeMapper;
    }

    public BloodPressurePage list(String empCode, String startTime, String endTime, Integer page, Integer size) {
        String code = requireEmpCode(empCode);
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        String start = blankToNull(startTime);
        String end = blankToNull(endTime);
        long total = bloodPressureMapper.countList(code, start, end);
        return new BloodPressurePage(
                bloodPressureMapper.findPage(code, start, end, offset, safeSize),
                total,
                safePage,
                safeSize);
    }

    public List<BloodPressureRecord> trend(String empCode, String startTime, String endTime) {
        String code = requireEmpCode(empCode);
        return bloodPressureMapper.findTrend(code, blankToNull(startTime), blankToNull(endTime));
    }

    public BloodPressureRecord create(String empCode, Integer systolic, Integer diastolic, String recordTime) {
        String code = requireEmpCode(empCode);
        Employee employee = employeeMapper.findByEmpCode(code);
        if (employee == null) {
            throw new IllegalArgumentException("职工不存在");
        }
        if (systolic == null || systolic < 40 || systolic > 300) {
            throw new IllegalArgumentException("收缩压必须在40到300之间");
        }
        if (diastolic == null || diastolic < 20 || diastolic > 200) {
            throw new IllegalArgumentException("舒张压必须在20到200之间");
        }
        if (systolic <= diastolic) {
            throw new IllegalArgumentException("收缩压必须高于舒张压");
        }

        String time = blankToNull(recordTime);
        if (time == null) {
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        String tableName = "health_record_" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        bloodPressureMapper.insertRecord(tableName, code, systolic, diastolic, time);

        BloodPressureRecord record = new BloodPressureRecord();
        record.setSystolic(systolic);
        record.setDiastolic(diastolic);
        record.setUserCode(code);
        record.setEmpCode(code);
        record.setEmpName(employee.getEmpName());
        record.setRecordTime(time);
        return record;
    }

    private String requireEmpCode(String empCode) {
        String code = empCode == null ? "" : empCode.trim();
        if (code.isEmpty()) {
            throw new IllegalArgumentException("工号不能为空");
        }
        return code;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
