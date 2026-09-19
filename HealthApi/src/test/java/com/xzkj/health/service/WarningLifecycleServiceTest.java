package com.xzkj.health.service;

import com.xzkj.health.mapper.WarningIncidentMapper;
import com.xzkj.health.mapper.WarningRecordMapper;
import com.xzkj.health.model.dto.WarningActionResult;
import com.xzkj.health.model.dto.WarningIncidentState;
import com.xzkj.health.model.dto.WarningRecordView;
import com.xzkj.health.model.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningLifecycleServiceTest {
    private static final String OCCURRED_AT = "2026-09-15 13:22:23";

    @Mock WarningQueryService warningQueryService;
    @Mock WarningRecordMapper warningRecordMapper;
    @Mock WarningIncidentMapper warningIncidentMapper;
    @Mock SysUserService sysUserService;
    @Mock CurrentOperatorService currentOperatorService;

    WarningLifecycleService service;
    WarningRecordView warning;

    @BeforeEach
    void setUp() {
        service = new WarningLifecycleService(warningQueryService, warningRecordMapper,
                warningIncidentMapper, sysUserService, currentOperatorService);
        warning = new WarningRecordView();
        warning.setId(1046L);
        warning.setCreateTime(OCCURRED_AT);
        warning.setHandled(false);
        when(warningQueryService.detail(1046L, OCCURRED_AT)).thenReturn(warning);
    }

    @Test
    void derivesNewStateWithoutCreatingIncidentDuringQuery() {
        WarningIncidentState result = service.state(1046L, OCCURRED_AT);

        assertEquals("NEW", result.getStatus());
        assertEquals(1046L, result.getWarningId());
    }

    @Test
    void acknowledgesNewIncidentAndRecordsAction() {
        WarningIncidentState acknowledged = state("ACKED");
        when(warningIncidentMapper.findState(1046L, OCCURRED_AT))
                .thenReturn(null, acknowledged);
        when(currentOperatorService.name()).thenReturn("管理员");

        WarningActionResult result = service.acknowledge(1046L, OCCURRED_AT, "已查看");

        assertEquals("ACK", result.getAction());
        assertEquals("ACKED", result.getIncident().getStatus());
        verify(warningIncidentMapper).insertState(1046L, OCCURRED_AT, "NEW");
        verify(warningIncidentMapper).updateStatus(1046L, OCCURRED_AT, "ACKED");
        verify(warningIncidentMapper).insertAction(anyString(), org.mockito.ArgumentMatchers.eq(1046L),
                org.mockito.ArgumentMatchers.eq(OCCURRED_AT), org.mockito.ArgumentMatchers.eq("ACK"),
                org.mockito.ArgumentMatchers.eq("管理员"), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("已查看"));
    }

    @Test
    void assignsOwnerAndSlaWhileKeepingAcknowledgedStatus() {
        when(warningIncidentMapper.findState(1046L, OCCURRED_AT))
                .thenReturn(state("NEW"), state("ACKED"));
        SysUser owner = new SysUser();
        owner.setId(9L);
        owner.setUsername("doctor9");
        owner.setRealName("王医生");
        owner.setStatus(0);
        when(sysUserService.getById(9L)).thenReturn(owner);
        when(currentOperatorService.name()).thenReturn("管理员");

        WarningActionResult result = service.assign(1046L, OCCURRED_AT, 9L, 30, "尽快处理");

        assertEquals("ACKED", result.getIncident().getStatus());
        verify(warningIncidentMapper).assign(1046L, OCCURRED_AT, 9L, "王医生", 30);
    }

    @Test
    void resolvingAlsoMarksLegacyWarningHandled() {
        when(warningIncidentMapper.findState(1046L, OCCURRED_AT))
                .thenReturn(state("ACKED"), state("RESOLVED"));
        when(warningRecordMapper.markHandled("warning_record_202609", 1046L, OCCURRED_AT,
                "管理员", "已回访")).thenReturn(1);
        when(currentOperatorService.name()).thenReturn("管理员");

        WarningActionResult result = service.resolve(1046L, OCCURRED_AT, "已回访");

        assertEquals("RESOLVED", result.getIncident().getStatus());
        verify(warningIncidentMapper).updateStatus(1046L, OCCURRED_AT, "RESOLVED");
    }

    @Test
    void closeRequiresResolvedStatus() {
        when(warningIncidentMapper.findState(1046L, OCCURRED_AT)).thenReturn(state("ACKED"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.close(1046L, OCCURRED_AT, "关闭"));

        assertEquals("只有已处理事件可以关闭", error.getMessage());
    }

    @Test
    void falseAlarmRequiresReason() {
        when(warningIncidentMapper.findState(1046L, OCCURRED_AT)).thenReturn(state("NEW"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.falseAlarm(1046L, OCCURRED_AT, "  "));

        assertEquals("误报原因不能为空", error.getMessage());
    }

    private WarningIncidentState state(String status) {
        WarningIncidentState state = new WarningIncidentState();
        state.setWarningId(1046L);
        state.setOccurredAt(OCCURRED_AT);
        state.setStatus(status);
        return state;
    }
}
