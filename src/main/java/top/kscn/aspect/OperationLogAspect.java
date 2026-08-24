package top.kscn.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.annotation.OperationLog;
import top.kscn.common.Result;
import top.kscn.entity.PublicFile;
import top.kscn.entity.UserFile;
import top.kscn.mapper.PublicFileMapper;
import top.kscn.mapper.UserFileMapper;
import top.kscn.service.LogService;
import top.kscn.utils.SecurityContextUtil;
import top.kscn.utils.JwtUtil;
import top.kscn.exception.CustomException;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {
    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final LogService logService;
    private final PublicFileMapper publicFileMapper;
    private final UserFileMapper userFileMapper;
    private final ObjectMapper objectMapper;

    @Value("${operation-log.enabled:true}")
    private boolean enabled;

    public OperationLogAspect(LogService logService, PublicFileMapper publicFileMapper, UserFileMapper userFileMapper,
                              ObjectMapper objectMapper) {
        this.logService = logService;
        this.publicFileMapper = publicFileMapper;
        this.userFileMapper = userFileMapper;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(operationLog)")
    public Object recordOperation(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        if (!enabled) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = findRequest(joinPoint.getArgs());
        HttpServletResponse response = findResponse(joinPoint.getArgs());
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Long resourceId = asLong(findParameter(args, parameterNames, operationLog.resourceIdParam()));
        String resourceName = resolveResourceName(operationLog, resourceId);
        if (resourceName == null) {
            resourceName = asString(findParameter(args, parameterNames, operationLog.resourceNameParam()));
        }
        Long userId = SecurityContextUtil.currentUserId();

        try {
            Object result = joinPoint.proceed();
            userId = resolveUserId(userId, result, operationLog);
            String detailsJson = buildDetailsJson(joinPoint, operationLog, parameterNames, args, resourceId,
                    resourceName, true, result, null);
            saveSafely(operationLog, userId, resourceId, resourceName, true,
                    "LOGIN".equals(operationLog.eventType()) ? "登录成功" : null,
                    response == null ? 200 : response.getStatus(), detailsJson, request);
            return result;
        } catch (Throwable ex) {
            String remark = ex.getMessage();
            if ("LOGIN".equals(operationLog.eventType())) {
                remark = "登录失败" + (remark == null || remark.isBlank() ? "" : ": " + remark);
            }
            int status = ex instanceof CustomException customException ? customException.getCode() : 500;
            String detailsJson = buildDetailsJson(joinPoint, operationLog, parameterNames, args, resourceId,
                    resourceName, false, null, ex);
            saveSafely(operationLog, userId, resourceId, resourceName, false, remark, status, detailsJson, request);
            throw ex;
        }
    }

    private Long resolveUserId(Long currentUserId, Object result, OperationLog operationLog) {
        if (currentUserId != null || !"LOGIN".equals(operationLog.eventType())) return currentUserId;
        if (!(result instanceof Result<?> response) || !(response.getData() instanceof String token)) return null;
        try {
            String userId = JwtUtil.parseToken(token).getId();
            return userId == null ? null : Long.valueOf(userId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void saveSafely(OperationLog operationLog, Long userId, Long resourceId, String resourceName,
                            boolean success, String remark, Integer httpStatus, String detailsJson,
                            HttpServletRequest request) {
        try {
            logService.recordOperation(operationLog.eventType(), operationLog.action(), operationLog.resourceType(),
                    userId, resourceId, resourceName, success, remark, httpStatus, detailsJson, request);
        } catch (Exception ex) {
            log.warn("Failed to write operation log for {}", operationLog.action(), ex);
        }
    }

    private HttpServletRequest findRequest(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest request) {
                return request;
            }
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private HttpServletResponse findResponse(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof HttpServletResponse response) return response;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getResponse();
    }

    private Object findParameter(Object[] args, String[] names, String name) {
        if (name == null || name.isBlank() || names == null) return null;
        for (int i = 0; i < names.length && i < args.length; i++) {
            if (name.equals(names[i])) return args[i];
        }
        return null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return null;
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String asString(Object value) {
        if (value instanceof MultipartFile file) return file.getOriginalFilename();
        return value == null ? null : String.valueOf(value);
    }

    private String resolveResourceName(OperationLog operationLog, Long resourceId) {
        if (resourceId == null) return null;
        try {
            if ("PUBLIC_FILE".equals(operationLog.resourceType())) {
                PublicFile file = publicFileMapper.selectById(resourceId);
                return file == null ? null : file.getFileName();
            }
            if ("USER_FILE".equals(operationLog.resourceType())) {
                UserFile file = userFileMapper.selectById(resourceId);
                return file == null ? null : file.getFileName();
            }
            return null;
        } catch (Exception ex) {
            log.warn("Failed to resolve operation log resource name for {} {}", operationLog.resourceType(), resourceId, ex);
            return null;
        }
    }

    private String buildDetailsJson(ProceedingJoinPoint joinPoint, OperationLog operationLog, String[] parameterNames,
                                    Object[] args, Long resourceId, String resourceName, boolean success,
                                    Object result, Throwable throwable) {
        try {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("method", joinPoint.getSignature().toShortString());
            details.put("success", success);

            if (resourceId != null || resourceName != null || !operationLog.resourceType().isBlank()) {
                Map<String, Object> resource = new LinkedHashMap<>();
                putIfPresent(resource, "type", operationLog.resourceType());
                putIfPresent(resource, "id", resourceId);
                putIfPresent(resource, "nameBefore", resourceName);
                details.put("resource", resource);
            }

            Map<String, Object> parameters = summarizeParameters(parameterNames, args);
            if (!parameters.isEmpty()) {
                details.put("parameters", parameters);
            }

            if (result != null) {
                details.put("result", summarizeResult(result));
            }
            if (throwable != null) {
                Map<String, Object> exception = new LinkedHashMap<>();
                exception.put("type", throwable.getClass().getSimpleName());
                putIfPresent(exception, "message", throwable.getMessage());
                details.put("exception", exception);
            }

            return objectMapper.writeValueAsString(details);
        } catch (Exception ex) {
            log.warn("Failed to build operation log details for {}", operationLog.action(), ex);
            return null;
        }
    }

    private Map<String, Object> summarizeParameters(String[] parameterNames, Object[] args) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (parameterNames == null) return parameters;
        for (int i = 0; i < parameterNames.length && i < args.length; i++) {
            Object value = summarizeValue(parameterNames[i], args[i]);
            if (value != null) {
                parameters.put(parameterNames[i], value);
            }
        }
        return parameters;
    }

    private Object summarizeValue(String name, Object value) {
        if (value == null) return null;
        if (isSensitiveName(name)) return "[REDACTED]";
        if (value instanceof HttpServletRequest || value instanceof HttpServletResponse) return null;
        if (value instanceof MultipartFile file) {
            Map<String, Object> fileInfo = new LinkedHashMap<>();
            putIfPresent(fileInfo, "originalFilename", file.getOriginalFilename());
            putIfPresent(fileInfo, "contentType", file.getContentType());
            fileInfo.put("size", file.getSize());
            return fileInfo;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) return value;
        if (value instanceof Collection<?> collection) return summarizeCollection(collection);
        if (value.getClass().isArray()) return summarizeArray(value);
        if (value instanceof Map<?, ?> map) return sanitizeMap(map);
        return sanitizePojo(value);
    }

    private Object summarizeResult(Object result) {
        if (result instanceof Result<?> response) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("code", response.getCode());
            putIfPresent(summary, "message", response.getMessage());
            Object data = response.getData();
            if (data != null && !(data instanceof String)) {
                summary.put("data", summarizeValue("data", data));
            }
            return summary;
        }
        return summarizeValue("result", result);
    }

    private Object summarizeCollection(Collection<?> collection) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("size", collection.size());
        summary.put("items", collection.stream().limit(20).map(item -> summarizeValue("item", item)).toList());
        return summary;
    }

    private Object summarizeArray(Object array) {
        int length = Array.getLength(array);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("size", length);
        java.util.List<Object> items = new java.util.ArrayList<>();
        for (int i = 0; i < length && i < 20; i++) {
            items.add(summarizeValue("item", Array.get(array, i)));
        }
        summary.put("items", items);
        return summary;
    }

    private Map<String, Object> sanitizeMap(Map<?, ?> map) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            sanitized.put(key, summarizeValue(key, entry.getValue()));
        }
        return sanitized;
    }

    private Object sanitizePojo(Object value) {
        try {
            Map<String, Object> raw = objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {});
            return sanitizeMap(raw);
        } catch (IllegalArgumentException ex) {
            return String.valueOf(value);
        }
    }

    private boolean isSensitiveName(String name) {
        if (name == null) return false;
        String normalized = name.toLowerCase();
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("captcha")
                || normalized.contains("authorization")
                || normalized.contains("cookie")
                || normalized.endsWith("code")
                || normalized.contains("emailcode");
    }

    private void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value != null && (!(value instanceof String string) || !string.isBlank())) {
            map.put(key, value);
        }
    }
}
