package com.xzkj.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xzkj.health.model.entity.DeviceUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeviceUserMapper extends BaseMapper<DeviceUser> {
    @Select("SELECT id, device_id, emp_id, real_name, bind_time, unbind_time, is_current, bind_type " +
            "FROM device_user WHERE device_id = #{deviceId} AND is_current = 1")
    DeviceUser selectCurrentByDevice(@Param("deviceId") Long deviceId);

    @Select("SELECT id, device_id, emp_id, real_name, bind_time, unbind_time, is_current, bind_type " +
            "FROM device_user WHERE emp_id = #{empId} AND is_current = 1")
    DeviceUser selectCurrentByEmployee(@Param("empId") Long empId);
}
