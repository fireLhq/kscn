package top.kscn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.common.Result;
import top.kscn.entity.PublicFile;
import top.kscn.service.FileStorageService;
import top.kscn.service.PublicFileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 公共文件管理控制器
 * 基于数据库的公共文件管理系统
 */
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/public-files")
public class PublicFileController {

    @Autowired
    private PublicFileService publicFileService;

    @Autowired
    private FileStorageService fileStorageService;

    // ==================== 文件上传 ====================

    /**
     * 检查文件是否可以秒传
     */
    @PostMapping("/upload/check")
    public Result<Map<String, Object>> checkInstantUpload(
            @RequestParam String fileMd5,
            @RequestParam Long fileSize) {
        Map<String, Object> result = publicFileService.checkInstantUpload(fileMd5, fileSize);
        return Result.success("检查完成", result);
    }

    /**
     * 上传文件（小文件直接上传）
     */
    @PostMapping("/upload")
    public Result<PublicFile> uploadFile(
            @RequestParam(required = false) Long parentId,
            @RequestParam String fileMd5,
            @RequestParam(required = false) String category,
            @RequestParam MultipartFile file) {
        PublicFile publicFile = publicFileService.uploadFile(parentId, file, fileMd5, category);
        return Result.success("文件上传成功", publicFile);
    }

    /**
     * 从已存在的物理文件创建公共文件（秒传）
     */
    @PostMapping("/upload/instant")
    public Result<PublicFile> instantUpload(
            @RequestParam(required = false) Long parentId,
            @RequestParam String fileName,
            @RequestParam Long storageId,
            @RequestParam(required = false) String category) {
        PublicFile publicFile = publicFileService.createFileFromStorage(parentId, fileName, storageId, category);
        return Result.success("秒传成功", publicFile);
    }

    /**
     * 初始化分片上传
     */
    @PostMapping("/upload/init")
    public Result<Map<String, Object>> initChunkedUpload(
            @RequestParam String fileName,
            @RequestParam Long fileSize,
            @RequestParam String fileMd5) {
        Map<String, Object> result = publicFileService.initChunkedUpload(fileName, fileSize, fileMd5);
        return Result.success("初始化成功", result);
    }

    /**
     * 上传分片
     */
    @PostMapping("/upload/chunk")
    public Result<?> uploadChunk(
            @RequestParam String uploadId,
            @RequestParam Integer chunkNumber,
            @RequestParam MultipartFile chunk) {
        fileStorageService.uploadChunk(uploadId, chunkNumber, chunk);
        return Result.success("分片上传成功", null);
    }

    /**
     * 完成分片上传
     */
    @PostMapping("/upload/complete")
    public Result<PublicFile> completeChunkedUpload(
            @RequestParam String uploadId,
            @RequestParam(required = false) Long parentId,
            @RequestParam String fileName,
            @RequestParam(required = false) String category) {
        PublicFile publicFile = publicFileService.completeChunkedUpload(parentId, fileName, uploadId, category);
        return Result.success("上传完成", publicFile);
    }

    /**
     * 取消分片上传
     */
    @PostMapping("/upload/cancel")
    public Result<?> cancelChunkedUpload(@RequestParam String uploadId) {
        fileStorageService.cancelChunkedUpload(uploadId);
        return Result.success("取消成功", null);
    }

    /**
     * 获取上传进度
     */
    @GetMapping("/upload/progress")
    public Result<Map<String, Object>> getUploadProgress(@RequestParam String uploadId) {
        Map<String, Object> progress = fileStorageService.getUploadProgress(uploadId);
        return Result.success("获取进度成功", progress);
    }

    // ==================== 文件夹操作 ====================

    /**
     * 创建文件夹
     */
    @PostMapping("/folder")
    public Result<PublicFile> createFolder(
            @RequestParam(required = false) Long parentId,
            @RequestParam String folderName) {
        PublicFile folder = publicFileService.createFolder(parentId, folderName);
        return Result.success("文件夹创建成功", folder);
    }

    /**
     * 获取文件列表（指定目录）
     */
    @GetMapping("/list")
    @PreAuthorize("permitAll()")
    public Result<List<PublicFile>> listFiles(@RequestParam(required = false) Long parentId) {
        List<PublicFile> files = publicFileService.listFiles(parentId);
        return Result.success("获取文件列表成功", files);
    }

    /**
     * 获取目录树结构（仅包含文件夹）
     */
    @GetMapping("/tree")
    @PreAuthorize("permitAll()")
    public Result<List<Map<String, Object>>> getDirectoryTree() {
        List<Map<String, Object>> tree = publicFileService.getDirectoryTree();
        return Result.success("获取目录树成功", tree);
    }

    /**
     * 获取所有文件
     */
    @GetMapping("/list/all")
    @PreAuthorize("permitAll()")
    public Result<List<PublicFile>> listAllFiles() {
        List<PublicFile> files = publicFileService.listAllFiles();
        return Result.success("获取文件列表成功", files);
    }

    /**
     * 根据分类获取文件
     */
    @GetMapping("/list/category")
    @PreAuthorize("permitAll()")
    public Result<List<PublicFile>> listFilesByCategory(@RequestParam String category) {
        List<PublicFile> files = publicFileService.listFilesByCategory(category);
        return Result.success("获取文件列表成功", files);
    }

    /**
     * 搜索文件
     */
    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public Result<List<PublicFile>> searchFiles(@RequestParam String keyword) {
        List<PublicFile> files = publicFileService.searchFiles(keyword);
        return Result.success("搜索完成", files);
    }

    /**
     * 根据ID获取文件
     */
    @GetMapping("/{fileId}")
    @PreAuthorize("permitAll()")
    public Result<PublicFile> getFileById(@PathVariable Long fileId) {
        PublicFile file = publicFileService.getFileById(fileId);
        return Result.success("获取文件成功", file);
    }

    // ==================== 文件操作 ====================

    /**
     * 删除文件（移入回收站）
     */
    @DeleteMapping("/delete")
    public Result<?> deleteFile(@RequestParam Long fileId) {
        publicFileService.deleteFile(fileId);
        return Result.success("文件已移入回收站", null);
    }

    /**
     * 彻底删除文件
     */
    @DeleteMapping("/delete/permanent")
    public Result<?> permanentDeleteFile(@RequestParam Long fileId) {
        publicFileService.permanentDeleteFile(fileId);
        return Result.success("文件已彻底删除", null);
    }

    /**
     * 重命名文件
     */
    @PutMapping("/rename")
    public Result<?> renameFile(
            @RequestParam Long fileId,
            @RequestParam String newName) {
        publicFileService.renameFile(fileId, newName);
        return Result.success("重命名成功", null);
    }

    /**
     * 移动文件
     */
    @PutMapping("/move")
    public Result<?> moveFile(
            @RequestParam Long fileId,
            @RequestParam(required = false) Long targetParentId) {
        publicFileService.moveFile(fileId, targetParentId);
        return Result.success("移动成功", null);
    }

    // ==================== 文件下载 ====================

    /**
     * 下载文件（流式下载，支持断点续传）
     */
    @GetMapping("/download")
    @PreAuthorize("permitAll()")
    public void downloadFile(
            @RequestParam Long fileId,
            @RequestHeader(value = "Range", required = false) String range,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        publicFileService.downloadFile(fileId, range, response);
    }

    /**
     * 下载文件夹（打包成ZIP）
     */
    @GetMapping("/download/folder/{folderId}")
    @PreAuthorize("permitAll()")
    public void downloadFolder(
            @PathVariable Long folderId,
            @RequestHeader(value = "Range", required = false) String range,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        publicFileService.downloadFolder(folderId, range, response);
    }

    /**
     * 批量下载文件（打包成ZIP）
     */
    @PostMapping("/download/batch")
    @PreAuthorize("permitAll()")
    public Result<Map<String, Object>> batchDownload(@RequestBody List<Long> fileIds) {
        Map<String, Object> result = publicFileService.createBatchDownloadTask(fileIds);
        return Result.success("批量下载任务创建成功", result);
    }

    /**
     * 下载批量下载任务的ZIP文件
     */
    @GetMapping("/download/batch/{taskId}")
    @PreAuthorize("permitAll()")
    public void downloadBatchTask(
            @PathVariable String taskId,
            @RequestHeader(value = "Range", required = false) String range,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        publicFileService.downloadBatchTask(taskId, range, response);
    }

    // ==================== 回收站 ====================

    /**
     * 获取回收站文件列表
     */
    @GetMapping("/recycle")
    public Result<List<PublicFile>> getRecycleBin() {
        List<PublicFile> files = publicFileService.getRecycleBin();
        return Result.success("获取回收站列表成功", files);
    }

    /**
     * 恢复文件
     */
    @PutMapping("/recycle/restore")
    public Result<?> restoreFile(@RequestParam Long fileId) {
        publicFileService.restoreFile(fileId);
        return Result.success("文件恢复成功", null);
    }

    /**
     * 清空回收站
     */
    @DeleteMapping("/recycle/empty")
    public Result<?> emptyRecycleBin() {
        publicFileService.emptyRecycleBin();
        return Result.success("回收站已清空", null);
    }

    // ==================== 统计信息 ====================

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("permitAll()")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = publicFileService.getStatistics();
        return Result.success("获取统计信息成功", stats);
    }

    @GetMapping("/count")
    @PreAuthorize("permitAll()")
    public Result<Long> countPublicResourceFiles() {
        return Result.success(null, publicFileService.countPublicResourceFiles());
    }
}
