package com.xzkj.health.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableNameUtilTest {
    @Test
    void routesWarningToItsMonthTable() {
        assertEquals("warning_record_202609",
                TableNameUtil.warningRecordTable(LocalDateTime.of(2026, 9, 30, 23, 59)));
    }
}
