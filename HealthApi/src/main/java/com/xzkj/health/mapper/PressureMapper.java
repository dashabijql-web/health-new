package com.xzkj.health.mapper;

import com.xzkj.health.model.dto.PressureRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PressureMapper {
    @Select({
            "<script>",
            "SELECT r.id, r.pressure, r.user_code,",
            "CONVERT(varchar(19), r.record_time, 120) AS record_time,",
            "e.emp_name, e.emp_code",
            "FROM v_health_record r",
            "LEFT JOIN employee e ON e.emp_code = r.user_code",
            "WHERE r.pressure IS NOT NULL AND r.pressure BETWEEN 30 AND 100",
            "  AND r.user_code = #{empCode}",
            "  <if test='startTime != null'> AND r.record_time &gt;= #{startTime} </if>",
            "  <if test='endTime != null'> AND r.record_time &lt; DATEADD(day, 1, CONVERT(date, #{endTime})) </if>",
            "ORDER BY r.record_time DESC, r.id DESC",
            "OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY",
            "</script>"
    })
    List<PressureRecord> findPage(@Param("empCode") String empCode,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM v_health_record r",
            "WHERE r.pressure IS NOT NULL AND r.pressure BETWEEN 30 AND 100",
            "  AND r.user_code = #{empCode}",
            "  <if test='startTime != null'> AND r.record_time &gt;= #{startTime} </if>",
            "  <if test='endTime != null'> AND r.record_time &lt; DATEADD(day, 1, CONVERT(date, #{endTime})) </if>",
            "</script>"
    })
    long countList(@Param("empCode") String empCode,
                   @Param("startTime") String startTime,
                   @Param("endTime") String endTime);

    @Select({
            "<script>",
            "SELECT TOP 200 r.id, r.pressure, r.user_code,",
            "CONVERT(varchar(19), r.record_time, 120) AS record_time,",
            "e.emp_name, e.emp_code",
            "FROM v_health_record r",
            "LEFT JOIN employee e ON e.emp_code = r.user_code",
            "WHERE r.pressure IS NOT NULL AND r.pressure BETWEEN 30 AND 100",
            "  AND r.user_code = #{empCode}",
            "  <if test='startTime != null'> AND r.record_time &gt;= #{startTime} </if>",
            "  <if test='endTime != null'> AND r.record_time &lt; DATEADD(day, 1, CONVERT(date, #{endTime})) </if>",
            "ORDER BY r.record_time ASC, r.id ASC",
            "</script>"
    })
    List<PressureRecord> findTrend(@Param("empCode") String empCode,
                                   @Param("startTime") String startTime,
                                   @Param("endTime") String endTime);

    @Insert("INSERT INTO ${tableName} (user_code, pressure, record_time, create_time) " +
            "VALUES (#{userCode}, #{pressure}, #{recordTime}, GETDATE())")
    int insertRecord(@Param("tableName") String tableName,
                     @Param("userCode") String userCode,
                     @Param("pressure") Integer pressure,
                     @Param("recordTime") String recordTime);
}
