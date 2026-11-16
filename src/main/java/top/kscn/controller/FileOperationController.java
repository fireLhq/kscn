package top.kscn.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.kscn.annotation.OperationLog;
import top.kscn.common.Result;
import top.kscn.entity.dto.FileOperationRequest;
import top.kscn.exception.CustomException;
import top.kscn.service.FileOperationService;
import top.kscn.utils.SecurityContextUtil;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping({"/api/public-files/operations", "/api/user-files/operations"})
public class FileOperationController {
    private final FileOperationService operations;

    private Long userId(HttpServletRequest request) {
        if (request.getServletPath().startsWith("/api/public-files/")) return null;
        Long userId = SecurityContextUtil.currentUserId();
        if (userId == null) throw new CustomException(401, "请先登录");
        return userId;
    }

    private Long actorId() {
        Long actor = SecurityContextUtil.currentUserId();
        if (actor == null) throw new CustomException(401, "请先登录");
        return actor;
    }

    @PostMapping("/preview")
    public Result<FileOperationService.Preview> preview(@Valid @RequestBody FileOperationRequest operation, HttpServletRequest request) {
        if (operation.getTargetSpace() != null)
            return Result.success("预览成功", operations.previewTransfer(userId(request), actorId(), operation));
        return Result.success("预览成功", operations.preview(userId(request), operation));
    }

    @PostMapping("/execute")
    @OperationLog(eventType = "FILE", action = "BATCH_OPERATION", resourceType = "FILE_BATCH")
    public Result<Map<String, Object>> execute(@Valid @RequestBody FileOperationRequest operation, HttpServletRequest request) {
        if (operation.getTargetSpace() != null)
            return Result.success("操作完成", operations.executeTransfer(userId(request), actorId(), operation));
        return Result.success("操作完成", operations.execute(userId(request), operation));
    }
}
