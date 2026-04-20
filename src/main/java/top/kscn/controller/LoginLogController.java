package top.kscn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.kscn.common.Result;
import top.kscn.service.LoginLogService;
import top.kscn.utils.SecurityContextUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/login-log")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    @GetMapping("/stats")
    public Result<?> getLoginStats() {
        Long userId = SecurityContextUtil.currentUserId();
        Map<String, Object> stats = loginLogService.getLoginStats(userId);
        return Result.success(null, stats);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/visitor-count")
    public Result<?> getVisitorCount() {
        Long visitorCount = loginLogService.getVisitorCount();
        Map<String, Object> data = new HashMap<>();
        data.put("visitorCount", visitorCount);
        return Result.success(null, data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recent-logins")
    public Result<?> getRecentLogins(@RequestParam(required = false) Integer limit) {
        List<Map<String, Object>> recentLogs = loginLogService.getRecentLogins(limit);
        return Result.success(null, recentLogs);
    }
}

