package com.xzkj.health.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TableNameUtil {
    private static final DateTimeFormatter MONTH_SUFFIX = DateTimeFormatter.ofPattern("yyyyMM");

    private TableNameUtil() {
    }

    public static String warningRecordTable(LocalDateTime time) {
        if (time == null) {
            throw new IllegalArgumentException("预警时间不能为空");
        }
        return "warning_record_" + time.format(MONTH_SUFFIX);
    }
}
