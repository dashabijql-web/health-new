package com.xzkj.health.service;

import com.xzkj.health.mapper.WarningRecordMapper;
import com.xzkj.health.model.dto.WarningRecordPage;
import com.xzkj.health.model.dto.WarningRecordView;
import com.xzkj.health.model.enums.WarningEventSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class WarningQueryService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;
    private static final DateTimeFormatter DETAIL_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WarningRecordMapper warningRecordMapper;

    public WarningQueryService(WarningRecordMapper warningRecordMapper) {
        this.warningRecordMapper = warningRecordMapper;
    }

    public WarningRecordPage list(String keyword, String eventSource, String warningLevel,
                                  Boolean handled, String startDate, String endDate,
                                  Integer page, Integer size) {
        String source = validateSource(blankToNull(eventSource));
        String level = validateLevel(blankToNull(warningLevel));
        String start = validateDate("开始日期", blankToNull(startDate));
        String end = validateDate("结束日期", blankToNull(endDate));
        if (start != null && end != null && LocalDate.parse(start).isAfter(LocalDate.parse(end))) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        String normalizedKeyword = blankToNull(keyword);
        long total = warningRecordMapper.countList(
                normalizedKeyword, source, level, handled, start, end);
        return new WarningRecordPage(
                warningRecordMapper.findPage(normalizedKeyword, source, level, handled,
                        start, end, offset, safeSize),
                total, safePage, safeSize);
    }

    public WarningRecordView detail(Long id, String createTime) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("预警ID必须为正整数");
        }
        String normalizedTime = blankToNull(createTime);
        if (normalizedTime == null) {
            throw new IllegalArgumentException("预警发生时间不能为空");
        }
        try {
            LocalDateTime.parse(normalizedTime, DETAIL_TIME);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("预警发生时间格式必须为yyyy-MM-dd HH:mm:ss");
        }
        WarningRecordView detail = warningRecordMapper.findDetail(id, normalizedTime);
        if (detail == null) {
            throw new IllegalArgumentException("预警记录不存在");
        }
        return detail;
    }

    private String validateSource(String source) {
        if (source == null) return null;
        try {
            WarningEventSource.valueOf(source);
            return source;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("未知的预警来源");
        }
    }

    private String validateLevel(String level) {
        if (level == null) return null;
        if (!level.equals("低危") && !level.equals("中危") && !level.equals("高危")) {
            throw new IllegalArgumentException("未知的预警级别");
        }
        return level;
    }

    private String validateDate(String label, String value) {
        if (value == null) return null;
        try {
            LocalDate.parse(value);
            return value;
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(label + "格式必须为yyyy-MM-dd");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
