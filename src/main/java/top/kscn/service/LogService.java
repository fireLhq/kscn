package top.kscn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import top.kscn.entity.Log;

import java.util.List;
import java.util.Map;

public interface LogService extends IService<Log> {
    void recordOperation(String eventType, String action, String resourceType, Long userId,
                         Long resourceId, String resourceName, boolean success, String remark,
                         Integer httpStatus, String detailsJson, HttpServletRequest request);

    Map<String, Object> getLoginStats(Long userId);

    Long getVisitorCount();

    List<Map<String, Object>> getRecentLogins(Integer limit, Integer days);

    Page<Log> getAdminLogs(Page<Log> page, String eventType, String action, Integer status, String keyword);

    Map<String, Object> getAdminLogDetail(Long logId);
}
