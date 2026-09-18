package com.xzkj.health.service;

import com.xzkj.health.mapper.BloodOxygenMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.model.dto.BloodOxygenPage;
import com.xzkj.health.model.dto.BloodOxygenRecord;
import com.xzkj.health.model.entity.Employee;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BloodOxygenService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;

    private final BloodOxygenMapper bloodOxygenMapper;
    private final EmployeeMapper employeeMapper;

    public BloodOxygenService(BloodOxygenMapper bloodOxygenMapper, EmployeeMapper employeeMapper) {
        this.bloodOxygenMapper = bloodOxygenMapper;
        this.employeeMapper = employeeMapper;
    }

    public BloodOxygenPage list(String empCode, String startTime, String endTime, Integer page, Integer size) {
        String code = requireEmpCode(empCode);
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        String start = blankToNull(startTime);
        String end = blankToNull(endTime);
        long total = bloodOxygenMapper.countList(code, start, end);
        return new BloodOxygenPage(
                bloodOxygenMapper.findPage(code, start, end, offset, safeSize),
                total,
                safePage,
                safeSize);
    }

    public List<BloodOxygenRecord> trend(String empCode, String startTime, String endTime) {
        String code = requireEmpCode(empCode);
        return bloodOxygenMapper.findTrend(code, blankToNull(startTime), blankToNull(endTime));
    }

    public BloodOxygenRecord create(String empCode, Integer bloodOxygen, String recordTime) {
        String code = requireEmpCode(empCode);
        Employee employee = employeeMapper.findByEmpCode(code);
        if (employee == null) {
            throw new IllegalArgumentException("职工不存在");
        }
        if (bloodOxygen == null || bloodOxygen < 50 || bloodOxygen > 100) {
            throw new IllegalArgumentException("血氧必须在50到100之间");
        }

        String time = blankToNull(recordTime);
        if (time == null) {
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        String tableName = "health_record_" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        bloodOxygenMapper.insertRecord(tableName, code, bloodOxygen, time);

        BloodOxygenRecord record = new BloodOxygenRecord();
        record.setBloodOxygen(bloodOxygen);
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
