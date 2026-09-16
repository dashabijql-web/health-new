package com.xzkj.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xzkj.health.model.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    @Select({
            "<script>",
            "SELECT id, parent_id, dept_name, dept_code, status, sort_order FROM department",
            "<where>",
            "  <if test='keyword != null'>",
            "    (dept_name LIKE CONCAT('%', #{keyword}, '%') OR dept_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY sort_order ASC, id ASC",
            "</script>"
    })
    List<Department> findList(@Param("keyword") String keyword);
}
