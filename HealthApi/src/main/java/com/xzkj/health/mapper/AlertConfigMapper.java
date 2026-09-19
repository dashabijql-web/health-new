package com.xzkj.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xzkj.health.model.entity.AlertConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlertConfigMapper extends BaseMapper<AlertConfig> {
    @Select({
            "SELECT id, config_name, config_type, unit, normal_min, normal_max,",
            "warn_low, warn_high, warn_mid_low, warn_mid_high, critical_low, critical_high,",
            "enabled, risk_level, create_time, update_time",
            "FROM alert_config",
            "ORDER BY config_type ASC,",
            "CASE WHEN risk_level IS NULL THEN 0 ELSE 1 END ASC,",
            "risk_level ASC, id ASC"
    })
    List<AlertConfig> findList();

    @Select({
            "<script>",
            "SELECT TOP 1 id, config_name, config_type, unit, normal_min, normal_max,",
            "warn_low, warn_high, warn_mid_low, warn_mid_high, critical_low, critical_high,",
            "enabled, risk_level, create_time, update_time",
            "FROM alert_config",
            "WHERE config_type = #{configType} AND enabled = 1",
            "<choose>",
            "  <when test='riskLevel != null'>",
            "    AND (risk_level = #{riskLevel} OR risk_level IS NULL)",
            "    ORDER BY CASE WHEN risk_level = #{riskLevel} THEN 0 ELSE 1 END, id ASC",
            "  </when>",
            "  <otherwise>",
            "    AND risk_level IS NULL",
            "    ORDER BY id ASC",
            "  </otherwise>",
            "</choose>",
            "</script>"
    })
    AlertConfig findEffective(@Param("configType") Integer configType,
                              @Param("riskLevel") Integer riskLevel);
}
