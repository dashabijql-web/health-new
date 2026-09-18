package com.xzkj.health.mapper;

import com.xzkj.health.model.dto.HeartRateRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HeartRateMapper {
    @Select({
            "<script>",
            "SELECT r.id, r.heart_rate, r.user_code,",
            "CONVERT(varchar(19), r.record_time, 120) AS record_time,",
            "e.emp_name, e.emp_code",
            "FROM v_health_record r",
            "LEFT JOIN employee e ON e.emp_code = r.user_code",
            "WHERE r.heart_rate IS NOT NULL AND r.user_code = #{empCode}",
            "  <if test='startTime != null'> AND r.record_time &gt;= #{startTime} </if>",
            "  <if test='endTime != null'> AND r.record_time &lt; DATEADD(day, 1, CONVERT(date, #{endTime})) </if>",
            "ORDER BY r.record_time DESC",
            "OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY",
            "</script>"
    })
    List<HeartRateRecord> findPage(@Param("empCode") String empCode,
                                   @Param("startTime") String startTime,
                                   @Param("endTime") String endTime,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM v_health_record r",
            "WHERE r.heart_rate IS NOT NULL AND r.user_code = #{empCode}",
            "  <if test='startTime != null'> AND r.record_time &gt;= #{startTime} </if>",
            "  <if test='endTime != null'> AND r.record_time &lt; DATEADD(day, 1, CONVERT(date, #{endTime})) </if>",
            "</script>"
    })
    long countList(@Param("empCode") String empCode,
                   @Param("startTime") String startTime,
                   @Param("endTime") String endTime);

    @Select({
            "<script>",
            "SELECT TOP 200 r.id, r.heart_rate, r.user_code,",
            "CONVERT(varchar(19), r.record_time, 120) AS record_time,",
            "e.emp_name, e.emp_code",
            "FROM v_health_record r",
            "LEFT JOIN employee e ON e.emp_code = r.user_code",
            "WHERE r.heart_rate IS NOT NULL AND r.user_code = #{empCode}",
            "  <if test='startTime != null'> AND r.record_time &gt;= #{startTime} </if>",
            "  <if test='endTime != null'> AND r.record_time &lt; DATEADD(day, 1, CONVERT(date, #{endTime})) </if>",
            "ORDER BY r.record_time ASC",
            "</script>"
    })
    List<HeartRateRecord> findTrend(@Param("empCode") String empCode,
                                    @Param("startTime") String startTime,
                                    @Param("endTime") String endTime);

    @Insert("INSERT INTO ${tableName} (user_code, heart_rate, record_time, create_time) " +
            "VALUES (#{userCode}, #{heartRate}, #{recordTime}, GETDATE())")
    int insertRecord(@Param("tableName") String tableName,
                     @Param("userCode") String userCode,
                     @Param("heartRate") Integer heartRate,
                     @Param("recordTime") String recordTime);
}
