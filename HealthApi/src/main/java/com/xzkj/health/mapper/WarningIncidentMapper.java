package com.xzkj.health.mapper;

import com.xzkj.health.model.dto.WarningIncidentState;
import com.xzkj.health.model.dto.WarningTimelineItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WarningIncidentMapper {
    @Select({
            "SELECT warning_id, CONVERT(varchar(19), occurred_at, 120) AS occurred_at, status,",
            "owner_user_id, owner_name, owner_dept,",
            "CONVERT(varchar(19), sla_due_at, 120) AS sla_due_at, sla_minutes,",
            "CONVERT(varchar(19), updated_at, 120) AS updated_at",
            "FROM command_center_incident",
            "WHERE warning_id = #{warningId} AND occurred_at = CONVERT(datetime2, #{occurredAt}, 120)"
    })
    WarningIncidentState findState(@Param("warningId") Long warningId,
                                   @Param("occurredAt") String occurredAt);

    @Insert({
            "INSERT INTO command_center_incident (warning_id, occurred_at, status)",
            "VALUES (#{warningId}, CONVERT(datetime2, #{occurredAt}, 120), #{status})"
    })
    int insertState(@Param("warningId") Long warningId,
                    @Param("occurredAt") String occurredAt,
                    @Param("status") String status);

    @Update({
            "UPDATE command_center_incident SET status = #{status}, updated_at = SYSDATETIME()",
            "WHERE warning_id = #{warningId} AND occurred_at = CONVERT(datetime2, #{occurredAt}, 120)"
    })
    int updateStatus(@Param("warningId") Long warningId,
                     @Param("occurredAt") String occurredAt,
                     @Param("status") String status);

    @Update({
            "UPDATE command_center_incident SET owner_user_id = #{ownerUserId}, owner_name = #{ownerName},",
            "sla_minutes = #{slaMinutes}, sla_due_at = DATEADD(minute, #{slaMinutes}, occurred_at),",
            "status = 'ACKED', updated_at = SYSDATETIME()",
            "WHERE warning_id = #{warningId} AND occurred_at = CONVERT(datetime2, #{occurredAt}, 120)"
    })
    int assign(@Param("warningId") Long warningId,
               @Param("occurredAt") String occurredAt,
               @Param("ownerUserId") Long ownerUserId,
               @Param("ownerName") String ownerName,
               @Param("slaMinutes") Integer slaMinutes);

    @Insert({
            "INSERT INTO command_center_incident_action",
            "(id, warning_id, occurred_at, action, result, operator_name, target, remark)",
            "VALUES (#{actionId}, #{warningId}, CONVERT(datetime2, #{occurredAt}, 120),",
            "#{action}, 'RECORDED', #{operator}, #{target}, #{remark})"
    })
    int insertAction(@Param("actionId") String actionId,
                     @Param("warningId") Long warningId,
                     @Param("occurredAt") String occurredAt,
                     @Param("action") String action,
                     @Param("operator") String operator,
                     @Param("target") String target,
                     @Param("remark") String remark);

    @Select({
            "SELECT CONVERT(varchar(36), id) AS action_id, action, result,",
            "operator_name AS operator, target, remark,",
            "CONVERT(varchar(19), created_at, 120) AS created_at",
            "FROM command_center_incident_action",
            "WHERE warning_id = #{warningId} AND occurred_at = CONVERT(datetime2, #{occurredAt}, 120)",
            "ORDER BY created_at ASC, id ASC"
    })
    List<WarningTimelineItem> findTimeline(@Param("warningId") Long warningId,
                                           @Param("occurredAt") String occurredAt);
}
