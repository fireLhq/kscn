package top.kscn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import top.kscn.entity.Log;
import top.kscn.mapper.LogMapper;
import top.kscn.mapper.PublicFileMapper;
import top.kscn.mapper.UserFileMapper;
import top.kscn.mapper.UserMapper;
import top.kscn.service.IpLocationService;
import top.kscn.service.LogService;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements LogService {
    private final IpLocationService ipLocationService;
    private final UserMapper userMapper;
    private final PublicFileMapper publicFileMapper;
    private final UserFileMapper userFileMapper;

    public LogServiceImpl(IpLocationService ipLocationService, UserMapper userMapper,
                          PublicFileMapper publicFileMapper, UserFileMapper userFileMapper) {
        this.ipLocationService = ipLocationService;
        this.userMapper = userMapper;
        this.publicFileMapper = publicFileMapper;
        this.userFileMapper = userFileMapper;
    }

    @Override
    public void recordOperation(String eventType, String action, String resourceType, Long userId,
                                Long resourceId, String resourceName, boolean success, String remark,
                                Integer httpStatus, String detailsJson, HttpServletRequest request) {
        Log log = new Log();
        log.setEventTime(new Date());
        log.setEventType(eventType);
        log.setAction(action);
        log.setUserId(userId);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setResourceName(resourceName);
        log.setStatus(success ? 1 : 0);
        log.setHttpStatus(httpStatus);
        log.setRemark(remark == null ? null : remark.substring(0, Math.min(remark.length(), 500)));
        log.setDetailsJson(detailsJson);
        if (request != null) {
            String userAgent = request.getHeader("User-Agent");
            log.setIp(getClientIp(request));
            log.setUserAgent(userAgent);
            log.setDeviceType(parseDeviceType(userAgent));
            log.setOs(parseOs(userAgent));
            log.setBrowser(parseBrowser(userAgent));
            log.setHttpMethod(request.getMethod());
            log.setRequestPath(request.getRequestURI());
        }
        save(log);
    }

    @Override
    public Map<String, Object> getLoginStats(Long userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date since = calendar.getTime();

        Long loginCount = baseMapper.countSuccessLoginsSince(userId, since);
        Log previousLogin = baseMapper.selectPreviousSuccessLogin(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("loginCount", loginCount == null ? 0L : loginCount);
        stats.put("lastActiveTime", previousLogin == null ? null : previousLogin.getEventTime());
        stats.put("lastLoginIp", previousLogin == null ? null : previousLogin.getIp());
        stats.put("lastLoginLocation", previousLogin == null ? null : ipLocationService.resolve(previousLogin.getIp()));
        return stats;
    }

    @Override
    public Long getVisitorCount() {
        Long count = baseMapper.countAllLogs();
        return count == null ? 0L : count;
    }

    @Override
    public List<Map<String, Object>> getRecentLogins(Integer limit, Integer days) {
        int size = (limit == null || limit < 1) ? 8 : Math.min(limit, 56);
        Date since = null;
        if (days != null && days > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            since = calendar.getTime();
        }
        return baseMapper.selectRecentSuccessLogins(size, since);
    }

    @Override
    public Page<Log> getAdminLogs(Page<Log> page, String eventType, String action, Integer status, String keyword) {
        QueryWrapper<Log> query = new QueryWrapper<>();
        if (eventType != null && !eventType.isBlank()) query.eq("event_type", eventType);
        if (action != null && !action.isBlank()) query.eq("action", action);
        if (status != null) query.eq("status", status);
        if (keyword != null && !keyword.isBlank()) {
            query.and(wrapper -> wrapper.like("ip", keyword)
                    .or().like("resource_name", keyword)
                    .or().like("remark", keyword)
                    .or().like("request_path", keyword));
        }
        query.orderByDesc("event_time");
        return page(page, query);
    }

    @Override
    public Map<String, Object> getAdminLogDetail(Long logId) {
        Log log = getById(logId);
        if (log == null) return null;
        Map<String, Object> result = new HashMap<>();
        result.put("log", log);
        if (log.getUserId() != null) {
            top.kscn.entity.User user = userMapper.selectById(log.getUserId());
            if (user != null) {
                user.setPassword(null);
                result.put("user", user);
            }
        }
        if (log.getResourceId() != null) {
            if ("PUBLIC_FILE".equals(log.getResourceType())) {
                result.put("resource", publicFileMapper.selectById(log.getResourceId()));
            } else if ("USER_FILE".equals(log.getResourceType())) {
                result.put("resource", userFileMapper.selectById(log.getResourceId()));
            }
        }
        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String parseDeviceType(String userAgent) {
        if (userAgent == null) return null;
        if (userAgent.contains("Mobile") || userAgent.contains("Android") && !userAgent.contains("Tablet")) return "Mobile";
        if (userAgent.contains("Tablet") || userAgent.contains("iPad")) return "Tablet";
        return "PC";
    }

    private String parseOs(String userAgent) {
        if (userAgent == null) return null;
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS X")) return "macOS";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        if (userAgent.contains("Linux")) return "Linux";
        return "Unknown";
    }

    private String parseBrowser(String userAgent) {
        if (userAgent == null) return null;
        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("Chrome") && !userAgent.contains("Chromium")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        if (userAgent.contains("OPR") || userAgent.contains("Opera")) return "Opera";
        return "Unknown";
    }
}
