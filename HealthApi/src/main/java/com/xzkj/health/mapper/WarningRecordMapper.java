package com.xzkj.health.mapper;

import com.xzkj.health.model.entity.WarningRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

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
}
