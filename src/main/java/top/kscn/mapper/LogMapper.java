package top.kscn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import top.kscn.entity.Log;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface LogMapper extends BaseMapper<Log> {

    @Select("SELECT COUNT(1) FROM `log` WHERE user_id = #{userId} AND event_type = 'LOGIN' AND status = 1 AND event_time >= #{since}")
    Long countSuccessLoginsSince(@Param("userId") Long userId, @Param("since") Date since);

    @Select("SELECT * FROM `log` WHERE user_id = #{userId} AND event_type = 'LOGIN' AND status = 1 ORDER BY event_time DESC LIMIT 1 OFFSET 1")
    Log selectPreviousSuccessLogin(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM `log` WHERE event_type = 'LOGIN'")
    Long countAllLogs();

    @SelectProvider(type = RecentLoginSqlProvider.class, method = "build")
    List<Map<String, Object>> selectRecentSuccessLogins(@Param("limit") Integer limit, @Param("since") Date since);

    class RecentLoginSqlProvider {
        public static String build() {
            return """
            SELECT
                l.log_id AS logId,
                l.user_id AS userId,
                u.username AS username,
                u.nickname AS nickname,
                u.email AS email,
                u.avatar AS avatar,
                l.event_time AS loginTime,
                l.ip AS ip,
                l.device_type AS deviceType,
                l.os AS os,
                l.browser AS browser,
                l.remark AS remark
            FROM `log` l
            LEFT JOIN user u ON u.user_id = l.user_id
                        WHERE l.event_type = 'LOGIN' AND l.status = 1
                            AND (#{since} IS NULL OR l.event_time >= #{since})
            ORDER BY l.event_time DESC
            LIMIT #{limit}
                        """;
                }
        }
}