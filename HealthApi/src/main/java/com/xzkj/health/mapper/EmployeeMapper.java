package com.xzkj.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xzkj.health.model.dto.EmployeeListItem;
import com.xzkj.health.model.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
    @Select({
            "<script>",
            "SELECT e.id, e.emp_name, e.emp_code, e.gender, e.phone, e.dept_id, e.job_type_id, e.status,",
            "d.dept_name AS dept_name, j.type_name AS job_type_name",
            "FROM employee e",
            "LEFT JOIN department d ON e.dept_id = d.id",
            "LEFT JOIN job_type j ON e.job_type_id = j.id",
            "<where>",
            "  <if test='keyword != null'>",
            "    (e.emp_name LIKE CONCAT('%', #{keyword}, '%') OR e.emp_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY e.id ASC",
            "OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY",
            "</script>"
    })
    List<EmployeeListItem> findPage(@Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM employee e",
            "<where>",
            "  <if test='keyword != null'>",
            "    (e.emp_name LIKE CONCAT('%', #{keyword}, '%') OR e.emp_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countList(@Param("keyword") String keyword);
}
