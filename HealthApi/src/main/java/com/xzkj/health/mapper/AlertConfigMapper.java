package com.xzkj.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xzkj.health.model.entity.AlertConfig;
import org.apache.ibatis.annotations.Mapper;
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
}
