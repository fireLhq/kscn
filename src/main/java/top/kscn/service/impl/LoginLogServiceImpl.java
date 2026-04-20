package top.kscn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import top.kscn.entity.LoginLog;
import top.kscn.mapper.LoginLogMapper;
import top.kscn.service.LoginLogService;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog>
        implements LoginLogService {

    @Override
    public void record(Long userId, int status, String remark, HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setLoginTime(new Date());
        log.setIp(getClientIp(request));
        log.setUserAgent(ua);
        log.setDeviceType(parseDeviceType(ua));
        log.setOs(parseOs(ua));
        log.setBrowser(parseBrowser(ua));
        log.setStatus(status);
        log.setRemark(remark);
        save(log);
    }

    @Override
    public Map<String, Object> getLoginStats(Long userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date since = calendar.getTime();

        Long loginCount = baseMapper.countSuccessLoginsSince(userId, since);
        LoginLog previousLogin = baseMapper.selectPreviousSuccessLogin(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("loginCount", loginCount == null ? 0L : loginCount);
        stats.put("lastActiveTime", previousLogin == null ? null : previousLogin.getLoginTime());
        stats.put("lastLoginIp", previousLogin == null ? null : previousLogin.getIp());
        return stats;
    }

    @Override
    public Long getVisitorCount() {
        Long count = baseMapper.countAllLogs();
        return count == null ? 0L : count;
    }

    @Override
    public List<Map<String, Object>> getRecentLogins(Integer limit) {
        int size = (limit == null || limit < 1) ? 8 : Math.min(limit, 20);
        return baseMapper.selectRecentSuccessLogins(size);
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

    private String parseDeviceType(String ua) {
        if (ua == null) return null;
        if (ua.contains("Mobile") || ua.contains("Android") && !ua.contains("Tablet")) return "Mobile";
        if (ua.contains("Tablet") || ua.contains("iPad")) return "Tablet";
        return "PC";
    }

    private String parseOs(String ua) {
        if (ua == null) return null;
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS X")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Unknown";
    }

    private String parseBrowser(String ua) {
        if (ua == null) return null;
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("Chrome") && !ua.contains("Chromium")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome")) return "Safari";
        if (ua.contains("OPR") || ua.contains("Opera")) return "Opera";
        return "Unknown";
    }
}
