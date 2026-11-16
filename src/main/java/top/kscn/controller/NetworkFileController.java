package top.kscn.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.kscn.annotation.OperationLog;
import top.kscn.common.Result;
import top.kscn.entity.dto.NetworkFileRequest;
import top.kscn.exception.CustomException;
import top.kscn.service.NetworkFileService;
import top.kscn.service.RemoteFileClient;
import top.kscn.utils.SecurityContextUtil;
import java.util.Map;

@RestController @RequiredArgsConstructor @Validated @PreAuthorize("isAuthenticated()")
@RequestMapping({"/api/public-files/network", "/api/user-files/network"})
public class NetworkFileController {
    private final NetworkFileService files;
    private Long owner(HttpServletRequest request) {
        Long actor = SecurityContextUtil.currentUserId();
        if (actor == null) throw new CustomException(401, "请先登录");
        return request.getServletPath().startsWith("/api/public-files/") ? null : actor;
    }
    public record Check(@NotBlank @Size(max = 8192) String url) {}
    @PostMapping("/check")
    public Result<RemoteFileClient.Probe> check(@Valid @RequestBody Check input, HttpServletRequest request) {
        owner(request); return Result.success("链接检查通过", files.check(input.url()));
    }
    @PostMapping
    @OperationLog(eventType = "FILE", action = "NETWORK_UPLOAD", resourceType = "NETWORK_FILE", resourceNameParam = "input")
    public Result<Map<String, Object>> create(@Valid @RequestBody NetworkFileRequest input, HttpServletRequest request) {
        return Result.success("添加成功", files.create(owner(request), input));
    }
}
