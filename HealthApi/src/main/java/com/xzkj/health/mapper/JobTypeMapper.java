package com.xzkj.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xzkj.health.model.entity.JobType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobTypeMapper extends BaseMapper<JobType> {
    @Select({
            "<script>",
            "SELECT id, type_name, type_code, risk_level, status FROM job_type",
            "<where>",
            "  <if test='keyword != null'>",
            "    (type_name LIKE CONCAT('%', #{keyword}, '%') OR type_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY id ASC",
            "</script>"
    })
    List<JobType> findList(@Param("keyword") String keyword);
}
