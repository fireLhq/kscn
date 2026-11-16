package top.kscn.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.kscn.common.Result;
import top.kscn.exception.CustomException;
import top.kscn.service.FilePropertiesService;
import top.kscn.service.FilePreviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import top.kscn.utils.SecurityContextUtil;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping({"/api/public-files", "/api/user-files"})
public class FilePropertiesController {
    private final FilePropertiesService properties;
    private final FilePreviewService preview;

    private Long userId(HttpServletRequest request) {
        if (request.getServletPath().startsWith("/api/public-files/")) return null;
        Long userId = SecurityContextUtil.currentUserId();
        if (userId == null) throw new CustomException(401, "请先登录");
        return userId;
    }

    @GetMapping("/{fileId}/properties")
    @PreAuthorize("permitAll()")
    public Result<FilePropertiesService.Properties> properties(@PathVariable @Positive Long fileId, HttpServletRequest request) {
        return Result.success("获取属性成功", properties.properties(userId(request), fileId));
    }

    @GetMapping("/{fileId}/hash")
    @PreAuthorize("permitAll()")
    public Result<FilePropertiesService.HashResult> hash(@PathVariable @Positive Long fileId,
            @RequestParam(defaultValue = "SHA-256") String algorithm, HttpServletRequest request) {
        return Result.success("校验完成", properties.hash(userId(request), fileId, algorithm));
    }

    @GetMapping("/{fileId}/preview")
    @PreAuthorize("permitAll()")
    public ResponseEntity<byte[]> preview(@PathVariable @Positive Long fileId, HttpServletRequest request) {
        var content = preview.content(userId(request), fileId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(content.mime()))
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "sandbox; default-src 'none';")
                .header("Cache-Control", "no-store")
                .body(content.bytes());
    }

    @GetMapping("/{fileId}/preview/office")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Result<FilePreviewService.OfficeView>> office(@PathVariable @Positive Long fileId, HttpServletRequest request) {
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(Result.success("预览成功", preview.office(userId(request), fileId)));
    }
}
