package com.xzkj.health.service;

import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.HeartRateMapper;
import com.xzkj.health.model.dto.HeartRatePage;
import com.xzkj.health.model.dto.HeartRateRecord;
import com.xzkj.health.model.entity.Employee;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HeartRateService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;

    private final HeartRateMapper heartRateMapper;
    private final EmployeeMapper employeeMapper;

    public HeartRateService(HeartRateMapper heartRateMapper, EmployeeMapper employeeMapper) {
        this.heartRateMapper = heartRateMapper;
        this.employeeMapper = employeeMapper;
    }

    public HeartRatePage list(String empCode, String startTime, String endTime, Integer page, Integer size) {
        String code = requireEmpCode(empCode);
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        String start = blankToNull(startTime);
        String end = blankToNull(endTime);
        long total = heartRateMapper.countList(code, start, end);
        return new HeartRatePage(heartRateMapper.findPage(code, start, end, offset, safeSize), total, safePage, safeSize);
    }

    public List<HeartRateRecord> trend(String empCode, String startTime, String endTime) {
        String code = requireEmpCode(empCode);
        return heartRateMapper.findTrend(code, blankToNull(startTime), blankToNull(endTime));
    }

    public HeartRateRecord create(String empCode, Integer heartRate, String recordTime) {
        String code = requireEmpCode(empCode);
        Employee employee = employeeMapper.findByEmpCode(code);
        if (employee == null) {
            throw new IllegalArgumentException("职工不存在");
        }
        if (heartRate == null || heartRate < 20 || heartRate > 300) {
            throw new IllegalArgumentException("心率必须在20到300之间");
        }
        String time = blankToNull(recordTime);
        if (time == null) {
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        String tableName = "health_record_" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        heartRateMapper.insertRecord(tableName, code, heartRate, time);
        HeartRateRecord record = new HeartRateRecord();
        record.setHeartRate(heartRate);
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
