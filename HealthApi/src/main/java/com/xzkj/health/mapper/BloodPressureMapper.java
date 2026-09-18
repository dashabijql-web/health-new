package com.xzkj.health.mapper;

import com.xzkj.health.model.dto.BloodPressureRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BloodPressureMapper {
    @Select({
            "<script>",
            "SELECT r.id, r.blood_pressure_high AS systolic, r.blood_pressure_low AS diastolic, r.user_code,",
            "CONVERT(varchar(19), r.record_time, 120) AS record_time,",
            "e.emp_name, e.emp_code",
            "FROM v_health_record r",
            "LEFT JOIN employee e ON e.emp_code = r.user_code",
            "WHERE r.blood_pressure_high IS NOT NULL AND r.blood_pressure_low IS NOT NULL",
            "  AND r.blood_pressure_high > 0 AND r.blood_pressure_low > 0",
            "  AND r.user_code = #{empCode}",
            "  <if test='startTime != null'> AND r.record_time &gt;= #{startTime} </if>",
            "  <if test='endTime != null'> AND r.record_time &lt; DATEADD(day, 1, CONVERT(date, #{endTime})) </if>",
            "ORDER BY r.record_time DESC, r.id DESC",
            "OFFSET #{offset} ROWS FETCH NEXT #{size} ROWS ONLY",
            "</script>"
    })
    List<BloodPressureRecord> findPage(@Param("empCode") String empCode,
                                       @Param("startTime") String startTime,
                                       @Param("endTime") String endTime,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM v_health_record r",
            "WHERE r.blood_pressure_high IS NOT NULL AND r.blood_pressure_low IS NOT NULL",
            "  AND r.blood_pressure_high > 0 AND r.blood_pressure_low > 0",
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
            "SELECT TOP 200 r.id, r.blood_pressure_high AS systolic, r.blood_pressure_low AS diastolic, r.user_code,",
            "CONVERT(varchar(19), r.record_time, 120) AS record_time,",
            "e.emp_name, e.emp_code",
            "FROM v_health_record r",
            "LEFT JOIN employee e ON e.emp_code = r.user_code",
            "WHERE r.blood_pressure_high IS NOT NULL AND r.blood_pressure_low IS NOT NULL",
            "  AND r.blood_pressure_high > 0 AND r.blood_pressure_low > 0",
            "  AND r.user_code = #{empCode}",
            "  <if test='startTime != null'> AND r.record_time &gt;= #{startTime} </if>",
            "  <if test='endTime != null'> AND r.record_time &lt; DATEADD(day, 1, CONVERT(date, #{endTime})) </if>",
            "ORDER BY r.record_time ASC, r.id ASC",
            "</script>"
    })
    List<BloodPressureRecord> findTrend(@Param("empCode") String empCode,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);

    @Insert("INSERT INTO ${tableName} (user_code, blood_pressure_high, blood_pressure_low, record_time, create_time) " +
            "VALUES (#{userCode}, #{systolic}, #{diastolic}, #{recordTime}, GETDATE())")
    int insertRecord(@Param("tableName") String tableName,
                     @Param("userCode") String userCode,
                     @Param("systolic") Integer systolic,
                     @Param("diastolic") Integer diastolic,
                     @Param("recordTime") String recordTime);
}
