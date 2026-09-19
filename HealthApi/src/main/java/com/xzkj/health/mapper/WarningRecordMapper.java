package com.xzkj.health.mapper;

import com.xzkj.health.model.entity.WarningRecord;
import com.xzkj.health.model.dto.WarningRecordView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WarningRecordMapper {
    @Insert({
            "INSERT INTO ${tableName}",
            "(user_code, warning_type, indicator_name, indicator_value, warning_level,",
            "event_source, event_code, device_imei, threshold_snapshot, is_handled, create_time)",
            "VALUES (#{record.userCode}, #{record.warningType}, #{record.indicatorName},",
            "#{record.indicatorValue}, #{record.warningLevel}, #{record.eventSource},",
            "#{record.eventCode}, #{record.deviceImei}, #{record.thresholdSnapshot}, 0, #{record.createTime})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "record.id")
    int insertToTable(@Param("tableName") String tableName,
                      @Param("record") WarningRecord record);

    String VIEW_COLUMNS = "wr.id, wr.user_code, e.emp_name, wr.warning_type, wr.indicator_name, "
            + "wr.indicator_value, wr.warning_level, wr.event_source, wr.event_code, wr.device_imei, "
            + "wr.threshold_snapshot, wr.is_handled AS handled, "
            + "CONVERT(varchar(19), wr.handle_time, 120) AS handle_time, wr.handle_by, wr.remark, "
            + "CONVERT(varchar(19), wr.create_time, 120) AS create_time";

    @Select({
            "<script>",
            "SELECT " + VIEW_COLUMNS,
            "FROM v_warning_record wr LEFT JOIN employee e ON e.emp_code = wr.user_code",
            "<where>",
            "  <if test='keyword != null'>",
            "    (wr.user_code LIKE CONCAT('%', #{keyword}, '%')",
            "     OR e.emp_name LIKE CONCAT('%', #{keyword}, '%')",
            "     OR wr.warning_type LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='eventSource != null'> AND wr.event_source = #{eventSource} </if>",
            "  <if test='warningLevel != null'> AND wr.warning_level = #{warningLevel} </if>",
            "  <if test='handled != null'> AND wr.is_handled = #{handled} </if>",
            "  <if test='startDate != null'> AND wr.create_time &gt;= #{startDate} </if>",
            "  <if test='endDate != null'> AND wr.create_time &lt; DATEADD(day, 1, CONVERT(date, #{endDate})) </if>",
            "</where>",
            "ORDER BY wr.create_time DESC, wr.id DESC",
            "OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY",
            "</script>"
    })
    List<WarningRecordView> findPage(@Param("keyword") String keyword,
                                     @Param("eventSource") String eventSource,
                                     @Param("warningLevel") String warningLevel,
                                     @Param("handled") Boolean handled,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM v_warning_record wr",
            "LEFT JOIN employee e ON e.emp_code = wr.user_code",
            "<where>",
            "  <if test='keyword != null'>",
            "    (wr.user_code LIKE CONCAT('%', #{keyword}, '%')",
            "     OR e.emp_name LIKE CONCAT('%', #{keyword}, '%')",
            "     OR wr.warning_type LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='eventSource != null'> AND wr.event_source = #{eventSource} </if>",
            "  <if test='warningLevel != null'> AND wr.warning_level = #{warningLevel} </if>",
            "  <if test='handled != null'> AND wr.is_handled = #{handled} </if>",
            "  <if test='startDate != null'> AND wr.create_time &gt;= #{startDate} </if>",
            "  <if test='endDate != null'> AND wr.create_time &lt; DATEADD(day, 1, CONVERT(date, #{endDate})) </if>",
            "</where>",
            "</script>"
    })
    long countList(@Param("keyword") String keyword,
                   @Param("eventSource") String eventSource,
                   @Param("warningLevel") String warningLevel,
                   @Param("handled") Boolean handled,
                   @Param("startDate") String startDate,
                   @Param("endDate") String endDate);

    @Select({
            "SELECT " + VIEW_COLUMNS,
            "FROM v_warning_record wr LEFT JOIN employee e ON e.emp_code = wr.user_code",
            "WHERE wr.id = #{id} AND wr.create_time = CONVERT(datetime, #{createTime}, 120)"
    })
    WarningRecordView findDetail(@Param("id") Long id,
                                 @Param("createTime") String createTime);

    @Update({
            "UPDATE ${tableName} SET is_handled = 1, handle_time = GETDATE(),",
            "handle_by = #{operator}, remark = #{remark}",
            "WHERE id = #{id} AND create_time = CONVERT(datetime, #{createTime}, 120) AND is_handled = 0"
    })
    int markHandled(@Param("tableName") String tableName,
                    @Param("id") Long id,
                    @Param("createTime") String createTime,
                    @Param("operator") String operator,
                    @Param("remark") String remark);
}
