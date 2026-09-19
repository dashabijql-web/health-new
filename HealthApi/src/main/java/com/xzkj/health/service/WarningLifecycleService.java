package com.xzkj.health.service;

import com.xzkj.health.mapper.WarningIncidentMapper;
import com.xzkj.health.mapper.WarningRecordMapper;
import com.xzkj.health.model.dto.WarningActionResult;
import com.xzkj.health.model.dto.WarningIncidentState;
import com.xzkj.health.model.dto.WarningRecordView;
import com.xzkj.health.model.dto.WarningTimelineItem;
import com.xzkj.health.model.entity.SysUser;
import com.xzkj.health.util.TableNameUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Service
public class WarningLifecycleService {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final WarningQueryService warningQueryService;
    private final WarningRecordMapper warningRecordMapper;
    private final WarningIncidentMapper warningIncidentMapper;
    private final SysUserService sysUserService;
    private final CurrentOperatorService currentOperatorService;

    public WarningLifecycleService(WarningQueryService warningQueryService,
                                   WarningRecordMapper warningRecordMapper,
                                   WarningIncidentMapper warningIncidentMapper,
                                   SysUserService sysUserService,
                                   CurrentOperatorService currentOperatorService) {
        this.warningQueryService = warningQueryService;
        this.warningRecordMapper = warningRecordMapper;
        this.warningIncidentMapper = warningIncidentMapper;
        this.sysUserService = sysUserService;
        this.currentOperatorService = currentOperatorService;
    }

    public WarningIncidentState state(Long warningId, String occurredAt) {
        WarningRecordView warning = warningQueryService.detail(warningId, occurredAt);
        WarningIncidentState state = warningIncidentMapper.findState(warningId, occurredAt);
        if (state != null) return state;
        return initialState(warning);
    }

    public List<WarningTimelineItem> timeline(Long warningId, String occurredAt) {
        warningQueryService.detail(warningId, occurredAt);
        return warningIncidentMapper.findTimeline(warningId, occurredAt);
    }

    @Transactional
    public WarningActionResult acknowledge(Long id, String occurredAt, String remark) {
        WarningRecordView warning = warningQueryService.detail(id, occurredAt);
        WarningIncidentState state = ensureState(warning);
        requireStatus(state, Set.of("NEW"), "只有新建事件可以确认");
        warningIncidentMapper.updateStatus(id, occurredAt, "ACKED");
        return record(id, occurredAt, "ACK", null, remark, "事件已确认");
    }

    @Transactional
    public WarningActionResult assign(Long id, String occurredAt, Long ownerUserId,
                                      Integer slaMinutes, String remark) {
        WarningRecordView warning = warningQueryService.detail(id, occurredAt);
        WarningIncidentState state = ensureState(warning);
        requireStatus(state, Set.of("NEW", "ACKED"), "当前状态不能分派");
        if (ownerUserId == null) throw new IllegalArgumentException("责任人不能为空");
        if (slaMinutes == null || slaMinutes < 1 || slaMinutes > 1440) {
            throw new IllegalArgumentException("SLA时限必须在1到1440分钟之间");
        }
        SysUser owner = sysUserService.getById(ownerUserId);
        if (owner == null || Integer.valueOf(1).equals(owner.getStatus())) {
            throw new IllegalArgumentException("责任人不存在或已禁用");
        }
        String ownerName = owner.getRealName() == null || owner.getRealName().isBlank()
                ? owner.getUsername() : owner.getRealName();
        warningIncidentMapper.assign(id, occurredAt, ownerUserId, ownerName, slaMinutes);
        return record(id, occurredAt, "ASSIGN", ownerName, remark, "事件已分派");
    }

    @Transactional
    public WarningActionResult resolve(Long id, String occurredAt, String remark) {
        WarningRecordView warning = warningQueryService.detail(id, occurredAt);
        WarningIncidentState state = ensureState(warning);
        requireStatus(state, Set.of("NEW", "ACKED"), "当前状态不能处理");
        markWarningHandled(warning, remark);
        warningIncidentMapper.updateStatus(id, occurredAt, "RESOLVED");
        return record(id, occurredAt, "RESOLVE", null, remark, "事件已处理");
    }

    @Transactional
    public WarningActionResult close(Long id, String occurredAt, String remark) {
        WarningRecordView warning = warningQueryService.detail(id, occurredAt);
        WarningIncidentState state = ensureState(warning);
        requireStatus(state, Set.of("RESOLVED"), "只有已处理事件可以关闭");
        warningIncidentMapper.updateStatus(id, occurredAt, "CLOSED");
        return record(id, occurredAt, "CLOSE", null, remark, "事件已关闭");
    }

    @Transactional
    public WarningActionResult falseAlarm(Long id, String occurredAt, String remark) {
        WarningRecordView warning = warningQueryService.detail(id, occurredAt);
        WarningIncidentState state = ensureState(warning);
        requireStatus(state, Set.of("NEW", "ACKED"), "当前状态不能标记误报");
        if (remark == null || remark.isBlank()) throw new IllegalArgumentException("误报原因不能为空");
        markWarningHandled(warning, remark);
        warningIncidentMapper.updateStatus(id, occurredAt, "FALSE_ALARM");
        return record(id, occurredAt, "FALSE_ALARM", null, remark, "事件已标记为误报");
    }

    private WarningIncidentState ensureState(WarningRecordView warning) {
        WarningIncidentState state = warningIncidentMapper.findState(warning.getId(), warning.getCreateTime());
        if (state == null) {
            String initial = Boolean.TRUE.equals(warning.getHandled()) ? "RESOLVED" : "NEW";
            warningIncidentMapper.insertState(warning.getId(), warning.getCreateTime(), initial);
            state = initialState(warning);
        }
        return state;
    }

    private WarningIncidentState initialState(WarningRecordView warning) {
        WarningIncidentState state = new WarningIncidentState();
        state.setWarningId(warning.getId());
        state.setOccurredAt(warning.getCreateTime());
        state.setStatus(Boolean.TRUE.equals(warning.getHandled()) ? "RESOLVED" : "NEW");
        return state;
    }

    private void requireStatus(WarningIncidentState state, Set<String> allowed, String message) {
        if (!allowed.contains(state.getStatus())) throw new IllegalArgumentException(message);
    }

    private void markWarningHandled(WarningRecordView warning, String remark) {
        LocalDateTime occurred = LocalDateTime.parse(warning.getCreateTime(), TIME);
        int rows = warningRecordMapper.markHandled(TableNameUtil.warningRecordTable(occurred),
                warning.getId(), warning.getCreateTime(), currentOperatorService.name(),
                remark == null ? "" : remark.trim());
        if (rows != 1) throw new IllegalStateException("预警已被其他操作处理");
    }

    private WarningActionResult record(Long id, String occurredAt, String action,
                                       String target, String remark, String message) {
        String actionId = UUID.randomUUID().toString();
        warningIncidentMapper.insertAction(actionId, id, occurredAt, action,
                currentOperatorService.name(), target, remark == null ? "" : remark.trim());
        return new WarningActionResult(actionId, action, message,
                warningIncidentMapper.findState(id, occurredAt));
    }
}
