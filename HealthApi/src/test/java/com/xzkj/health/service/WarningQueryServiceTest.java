package com.xzkj.health.service;

import com.xzkj.health.mapper.WarningRecordMapper;
import com.xzkj.health.model.dto.WarningRecordPage;
import com.xzkj.health.model.dto.WarningRecordView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningQueryServiceTest {
    @Mock
    WarningRecordMapper warningRecordMapper;

    @InjectMocks
    WarningQueryService warningQueryService;

    @Test
    void returnsFilteredPagedWarnings() {
        WarningRecordView item = record(1046L, "2026-09-15 13:22:23");
        when(warningRecordMapper.countList("EMP", "HEALTH_THRESHOLD", "高危", false,
                "2026-09-01", "2026-09-19")).thenReturn(1L);
        when(warningRecordMapper.findPage("EMP", "HEALTH_THRESHOLD", "高危", false,
                "2026-09-01", "2026-09-19", 0, 20)).thenReturn(List.of(item));

        WarningRecordPage result = warningQueryService.list(
                " EMP ", "HEALTH_THRESHOLD", "高危", false,
                "2026-09-01", "2026-09-19", 1, 20);

        assertEquals(1L, result.getTotal());
        assertEquals(item, result.getList().getFirst());
    }

    @Test
    void capsPageSizeAtFifty() {
        when(warningRecordMapper.countList(null, null, null, null, null, null)).thenReturn(0L);
        when(warningRecordMapper.findPage(null, null, null, null, null, null, 50, 50))
                .thenReturn(List.of());

        WarningRecordPage result = warningQueryService.list(
                null, null, null, null, null, null, 2, 100);

        assertEquals(50, result.getSize());
    }

    @Test
    void rejectsInvalidFilters() {
        assertEquals("未知的预警来源", assertThrows(IllegalArgumentException.class,
                () -> warningQueryService.list(null, "SOS", null, null,
                        null, null, 1, 20)).getMessage());
        assertEquals("未知的预警级别", assertThrows(IllegalArgumentException.class,
                () -> warningQueryService.list(null, null, "危急", null,
                        null, null, 1, 20)).getMessage());
        assertEquals("开始日期不能晚于结束日期", assertThrows(IllegalArgumentException.class,
                () -> warningQueryService.list(null, null, null, null,
                        "2026-09-20", "2026-09-01", 1, 20)).getMessage());
    }

    @Test
    void findsDetailByCompositeLocator() {
        WarningRecordView item = record(7L, "2026-08-01 08:30:00");
        when(warningRecordMapper.findDetail(7L, "2026-08-01 08:30:00")).thenReturn(item);

        assertEquals(item, warningQueryService.detail(7L, "2026-08-01 08:30:00"));
    }

    @Test
    void rejectsMissingDetailWithoutTryingAnotherMonth() {
        when(warningRecordMapper.findDetail(7L, "2026-08-01 08:30:00")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warningQueryService.detail(7L, "2026-08-01 08:30:00"));

        assertEquals("预警记录不存在", exception.getMessage());
    }

    @Test
    void rejectsDetailWithoutValidCompositeTime() {
        assertThrows(IllegalArgumentException.class, () -> warningQueryService.detail(7L, null));
        assertThrows(IllegalArgumentException.class, () -> warningQueryService.detail(7L, "2026-08-01"));
        verify(warningRecordMapper, never()).findDetail(7L, "2026-08-01");
    }

    private static WarningRecordView record(Long id, String createTime) {
        WarningRecordView item = new WarningRecordView();
        item.setId(id);
        item.setUserCode("EMP0001");
        item.setCreateTime(createTime);
        return item;
    }
}
