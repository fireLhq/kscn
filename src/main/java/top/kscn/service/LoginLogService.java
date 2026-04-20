package top.kscn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.kscn.entity.LoginLog;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface LoginLogService extends IService<LoginLog> {
    void record(Long userId, int status, String remark, HttpServletRequest request);

    Map<String, Object> getLoginStats(Long userId);

    Long getVisitorCount();

    List<Map<String, Object>> getRecentLogins(Integer limit);
}
