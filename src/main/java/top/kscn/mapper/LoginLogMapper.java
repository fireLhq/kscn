package top.kscn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.kscn.entity.LoginLog;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    @Select("SELECT COUNT(1) FROM login_log WHERE user_id = #{userId} AND status = 1 AND login_time >= #{since}")
    Long countSuccessLoginsSince(@Param("userId") Long userId, @Param("since") Date since);

    @Select("SELECT * FROM login_log WHERE user_id = #{userId} AND status = 1 ORDER BY login_time DESC LIMIT 1 OFFSET 1")
    LoginLog selectPreviousSuccessLogin(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM login_log")
    Long countAllLogs();

    @Select("""
            SELECT
                l.log_id AS logId,
                l.user_id AS userId,
                u.username AS username,
                u.nickname AS nickname,
                u.email AS email,
                u.avatar AS avatar,
                l.login_time AS loginTime,
                l.ip AS ip,
                l.device_type AS deviceType,
                l.os AS os,
                l.browser AS browser,
                l.remark AS remark
            FROM login_log l
            LEFT JOIN user u ON u.user_id = l.user_id
            WHERE l.status = 1
            ORDER BY l.login_time DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectRecentSuccessLogins(@Param("limit") Integer limit);
}

