package com.xzkj.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xzkj.health.model.dto.DeviceListItem;
import com.xzkj.health.model.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
    @Select({
            "<script>",
            "SELECT d.id, d.imei, d.device_type, d.status, d.online_status, d.battery_level,",
            "CONVERT(varchar(19), d.last_online_time, 120) AS last_online_time,",
            "du.emp_id, e.emp_name, e.emp_code",
            "FROM device d",
            "LEFT JOIN device_user du ON du.device_id = d.id AND du.is_current = 1",
            "LEFT JOIN employee e ON du.emp_id = e.id",
            "<where>",
            "  <if test='keyword != null'>",
            "    (d.imei LIKE CONCAT('%', #{keyword}, '%') OR e.emp_name LIKE CONCAT('%', #{keyword}, '%') OR e.emp_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY d.id ASC",
            "OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY",
            "</script>"
    })
    List<DeviceListItem> findPage(@Param("keyword") String keyword,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM device d",
            "LEFT JOIN device_user du ON du.device_id = d.id AND du.is_current = 1",
            "LEFT JOIN employee e ON du.emp_id = e.id",
            "<where>",
            "  <if test='keyword != null'>",
            "    (d.imei LIKE CONCAT('%', #{keyword}, '%') OR e.emp_name LIKE CONCAT('%', #{keyword}, '%') OR e.emp_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countList(@Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM device WHERE imei = #{imei}",
            "  <if test='excludeId != null'> AND id != #{excludeId} </if>",
            "</script>"
    })
    long countByImei(@Param("imei") String imei, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM device_user WHERE device_id = #{deviceId} AND is_current = 1")
    long countCurrentBindings(@Param("deviceId") Long deviceId);
}
