package com.xzkj.health.service;

import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.mapper.TemperatureMapper;
import com.xzkj.health.model.dto.TemperaturePage;
import com.xzkj.health.model.dto.TemperatureRecord;
import com.xzkj.health.model.entity.Employee;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TemperatureService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;
    private static final BigDecimal MIN_TEMPERATURE = new BigDecimal("35.0");
    private static final BigDecimal MAX_TEMPERATURE = new BigDecimal("42.0");

    private final TemperatureMapper temperatureMapper;
    private final EmployeeMapper employeeMapper;

    public TemperatureService(TemperatureMapper temperatureMapper, EmployeeMapper employeeMapper) {
        this.temperatureMapper = temperatureMapper;
        this.employeeMapper = employeeMapper;
    }

    public TemperaturePage list(String empCode, String startTime, String endTime, Integer page, Integer size) {
        String code = requireEmpCode(empCode);
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        String start = blankToNull(startTime);
        String end = blankToNull(endTime);
        long total = temperatureMapper.countList(code, start, end);
        return new TemperaturePage(
                temperatureMapper.findPage(code, start, end, offset, safeSize),
                total,
                safePage,
                safeSize);
    }

    public List<TemperatureRecord> trend(String empCode, String startTime, String endTime) {
        String code = requireEmpCode(empCode);
        return temperatureMapper.findTrend(code, blankToNull(startTime), blankToNull(endTime));
    }

    public TemperatureRecord create(String empCode, BigDecimal temperature, String recordTime) {
        String code = requireEmpCode(empCode);
        Employee employee = employeeMapper.findByEmpCode(code);
        if (employee == null) {
            throw new IllegalArgumentException("职工不存在");
        }
        if (temperature == null
                || temperature.compareTo(MIN_TEMPERATURE) < 0
                || temperature.compareTo(MAX_TEMPERATURE) > 0) {
            throw new IllegalArgumentException("体温必须在35.0到42.0℃之间");
        }

        int storedTemperature = temperature
                .movePointRight(1)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
        BigDecimal normalizedTemperature = BigDecimal.valueOf(storedTemperature, 1);
        String time = blankToNull(recordTime);
        if (time == null) {
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        String tableName = "health_record_" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        temperatureMapper.insertRecord(tableName, code, storedTemperature, time);

        TemperatureRecord record = new TemperatureRecord();
        record.setTemperature(normalizedTemperature);
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
