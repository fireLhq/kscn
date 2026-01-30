package top.kscn.controller;

import top.kscn.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.service.ChunkedFileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files/chunked")
public class ChunkedFileController {

    @Autowired
    private ChunkedFileService chunkedFileService;

    /**
     * 初始化分片上传
     */
    @PostMapping("/upload/init")
    public Result<Map<String, Object>> initChunkedUpload(
            @RequestParam String fileName,
            @RequestParam Long fileSize,
            @RequestParam String fileMd5,
            @RequestParam(defaultValue = "/") String path) {
        
        Map<String, Object> result = chunkedFileService.initChunkedUpload(fileName, fileSize, fileMd5, path);
        return Result.success("初始化分片上传成功", result);
    }

    /**
     * 上传分片
     */
    @PostMapping("/upload/chunk")
    public Result<?> uploadChunk(
            @RequestParam String uploadId,
            @RequestParam Integer chunkNumber,
            @RequestParam MultipartFile chunk) {
        
        chunkedFileService.uploadChunk(uploadId, chunkNumber, chunk);
        return Result.success("分片上传成功");
    }

    /**
     * 完成分片上传
     */
    @PostMapping("/upload/complete")
    public Result<?> completeChunkedUpload(@RequestParam String uploadId) {
        String filePath = chunkedFileService.completeChunkedUpload(uploadId);
        return Result.success("文件上传完成", filePath);
    }

    /**
     * 取消分片上传
     */
    @PostMapping("/upload/cancel")
    public Result<?> cancelChunkedUpload(@RequestParam String uploadId) {
        chunkedFileService.cancelChunkedUpload(uploadId);
        return Result.success("取消上传成功");
    }

    /**
     * 获取上传进度
     */
    @GetMapping("/upload/progress")
    public Result<Map<String, Object>> getUploadProgress(@RequestParam String uploadId) {
        Map<String, Object> progress = chunkedFileService.getUploadProgress(uploadId);
        return Result.success("获取进度成功", progress);
    }

    /**
     * 分片下载文件
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String path,
            @RequestHeader(value = "Range", required = false) String range) {
        
        try {
            // 使用服务层获取文件信息，确保路径安全
            Map<String, Object> fileInfo = chunkedFileService.getFileInfo(path);
            if (!(Boolean) fileInfo.get("exists")) {
                return ResponseEntity.notFound().build();
            }

            // 重新解析路径以确保安全
            Path filePath = chunkedFileService.resolveSafePath(path);
            File file = filePath.toFile();

            long fileLength = file.length();
            long start = 0;
            long end = fileLength - 1;

            // 处理Range请求
            if (range != null && range.startsWith("bytes=")) {
                String[] ranges = range.substring(6).split("-");
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
            }

            long contentLength = end - start + 1;
            Resource resource = new FileSystemResource(file);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文件信息（用于分片下载）
     */
    @GetMapping("/download/info")
    public Result<Map<String, Object>> getFileInfo(@RequestParam String path) {
        Map<String, Object> fileInfo = chunkedFileService.getFileInfo(path);
        return Result.success("获取文件信息成功", fileInfo);
    }
}
