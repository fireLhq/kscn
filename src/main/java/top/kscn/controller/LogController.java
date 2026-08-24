package top.kscn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.kscn.entity.Log;
import top.kscn.common.Result;
import top.kscn.service.LogService;
import top.kscn.utils.SecurityContextUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/login-log")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/stats")
    public Result<?> getLoginStats() {
        Long userId = SecurityContextUtil.currentUserId();
        Map<String, Object> stats = logService.getLoginStats(userId);
        return Result.success(null, stats);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/visitor-count")
    public Result<?> getVisitorCount() {
        Long visitorCount = logService.getVisitorCount();
        Map<String, Object> data = new HashMap<>();
        data.put("visitorCount", visitorCount);
        return Result.success(null, data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recent-logins")
    public Result<?> getRecentLogins(@RequestParam(required = false) Integer limit,
                                     @RequestParam(required = false) Integer days) {
        List<Map<String, Object>> recentLogs = logService.getRecentLogins(limit, days);
        return Result.success(null, recentLogs);
    }

    @GetMapping("/admin-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> getAdminLogs(@RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) String eventType,
                                  @RequestParam(required = false) String action,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(required = false) String keyword) {
        Page<Log> result = logService.getAdminLogs(new Page<>(page, Math.min(size, 100)), eventType, action, status, keyword);
        return Result.success(null, result);
    }

    @GetMapping("/admin-logs/{logId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> getAdminLogDetail(@PathVariable Long logId) {
        Map<String, Object> detail = logService.getAdminLogDetail(logId);
        return detail == null ? Result.error(404, "日志不存在") : Result.success(null, detail);
    }
}