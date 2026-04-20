package top.kscn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.common.Result;
import top.kscn.entity.UserFile;
import top.kscn.service.FileStorageService;
import top.kscn.service.UserFileService;
import top.kscn.utils.SecurityContextUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用户文件管理控制器
 * 基于数据库的用户文件管理系统
 * userId 自动从 token 中解析
 */
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/user-files")
public class UserFileController {

    @Autowired
    private UserFileService userFileService;

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
        Long userId = SecurityContextUtil.currentUserId();
        Map<String, Object> result = userFileService.checkInstantUpload(userId, fileMd5, fileSize);
        return Result.success("检查完成", result);
    }

    /**
     * 上传文件（小文件直接上传）
     */
    @PostMapping("/upload")
    public Result<UserFile> uploadFile(
            @RequestParam(required = false) Long parentId,
            @RequestParam String fileMd5,
            @RequestParam MultipartFile file) {
        Long userId = SecurityContextUtil.currentUserId();
        UserFile userFile = userFileService.uploadFile(userId, parentId, file, fileMd5);
        return Result.success("文件上传成功", userFile);
    }

    /**
     * 从已存在的物理文件创建用户文件（秒传）
     */
    @PostMapping("/upload/instant")
    public Result<UserFile> instantUpload(
            @RequestParam(required = false) Long parentId,
            @RequestParam String fileName,
            @RequestParam Long storageId) {
        Long userId = SecurityContextUtil.currentUserId();
        UserFile userFile = userFileService.createFileFromStorage(userId, parentId, fileName, storageId);
        return Result.success("秒传成功", userFile);
    }

    /**
     * 初始化分片上传
     */
    @PostMapping("/upload/init")
    public Result<Map<String, Object>> initChunkedUpload(
            @RequestParam String fileName,
            @RequestParam Long fileSize,
            @RequestParam String fileMd5) {
        Map<String, Object> result = userFileService.initChunkedUpload(fileName, fileSize, fileMd5);
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
    public Result<UserFile> completeChunkedUpload(
            @RequestParam String uploadId,
            @RequestParam(required = false) Long parentId,
            @RequestParam String fileName) {
        Long userId = SecurityContextUtil.currentUserId();
        UserFile userFile = userFileService.completeChunkedUpload(userId, parentId, fileName, uploadId);
        return Result.success("上传完成", userFile);
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
    public Result<UserFile> createFolder(
            @RequestParam(required = false) Long parentId,
            @RequestParam String folderName) {
        Long userId = SecurityContextUtil.currentUserId();
        UserFile folder = userFileService.createFolder(userId, parentId, folderName);
        return Result.success("文件夹创建成功", folder);
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public Result<List<UserFile>> listFiles(@RequestParam(required = false) Long parentId) {
        Long userId = SecurityContextUtil.currentUserId();
        List<UserFile> files = userFileService.listFiles(userId, parentId);
        return Result.success("获取文件列表成功", files);
    }

    /**
     * 获取目录树结构（仅包含文件夹）
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getDirectoryTree() {
        Long userId = SecurityContextUtil.currentUserId();
        List<Map<String, Object>> tree = userFileService.getDirectoryTree(userId);
        return Result.success("获取目录树成功", tree);
    }

    /**
     * 根据路径获取文件夹ID
     */
    @GetMapping("/folder/id")
    public Result<Long> getFolderIdByPath(@RequestParam String path) {
        Long userId = SecurityContextUtil.currentUserId();
        Long folderId = userFileService.getFolderIdByPath(userId, path);
        return Result.success("获取文件夹ID成功", folderId);
    }

    // ==================== 文件操作 ====================

    /**
     * 删除文件（移入回收站）
     */
    @DeleteMapping("/delete")
    public Result<?> deleteFile(@RequestParam Long fileId) {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.deleteFile(userId, fileId);
        return Result.success("文件已移入回收站", null);
    }

    /**
     * 彻底删除文件
     */
    @DeleteMapping("/delete/permanent")
    public Result<?> permanentDeleteFile(@RequestParam Long fileId) {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.permanentDeleteFile(userId, fileId);
        return Result.success("文件已彻底删除", null);
    }

    /**
     * 重命名文件
     */
    @PutMapping("/rename")
    public Result<?> renameFile(
            @RequestParam Long fileId,
            @RequestParam String newName) {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.renameFile(userId, fileId, newName);
        return Result.success("重命名成功", null);
    }

    /**
     * 移动文件
     */
    @PutMapping("/move")
    public Result<?> moveFile(
            @RequestParam Long fileId,
            @RequestParam(required = false) Long targetParentId) {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.moveFile(userId, fileId, targetParentId);
        return Result.success("移动成功", null);
    }

    /**
     * 搜索文件
     */
    @GetMapping("/search")
    public Result<List<UserFile>> searchFiles(@RequestParam String keyword) {
        Long userId = SecurityContextUtil.currentUserId();
        List<UserFile> files = userFileService.searchFiles(userId, keyword);
        return Result.success("搜索完成", files);
    }

    // ==================== 文件下载 ====================

    /**
     * 下载文件（流式下载，支持断点续传）
     */
    @GetMapping("/download")
    public void downloadFile(
            @RequestParam Long fileId,
            @RequestHeader(value = "Range", required = false) String range,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.downloadFile(userId, fileId, range, response);
    }

    /**
     * 下载文件夹（打包成ZIP）
     */
    @GetMapping("/download/folder/{folderId}")
    public void downloadFolder(
            @PathVariable Long folderId,
            @RequestHeader(value = "Range", required = false) String range,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.downloadFolder(userId, folderId, range, response);
    }

    /**
     * 批量下载文件（打包成ZIP）
     */
    @PostMapping("/download/batch")
    public Result<Map<String, Object>> batchDownload(@RequestBody List<Long> fileIds) {
        Long userId = SecurityContextUtil.currentUserId();
        Map<String, Object> result = userFileService.createBatchDownloadTask(userId, fileIds);
        return Result.success("批量下载任务创建成功", result);
    }

    /**
     * 下载批量下载任务的ZIP文件
     */
    @GetMapping("/download/batch/{taskId}")
    public void downloadBatchTask(
            @PathVariable String taskId,
            @RequestHeader(value = "Range", required = false) String range,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.downloadBatchTask(userId, taskId, range, response);
    }

    // ==================== 空间管理 ====================

    /**
     * 获取用户空间使用情况
     */
    @GetMapping("/space")
    public Result<Map<String, Object>> getUserSpaceInfo() {
        Long userId = SecurityContextUtil.currentUserId();
        Map<String, Object> spaceInfo = userFileService.getUserSpaceInfo(userId);
        return Result.success("获取空间信息成功", spaceInfo);
    }

    /**
     * 获取用户文件数量
     */
    @GetMapping("/count")
    public Result<Long> getUserFileCount() {
        Long userId = SecurityContextUtil.currentUserId();
        Long fileCount = userFileService.getUserFileCount(userId);
        return Result.success("获取文件数量成功", fileCount);
    }

    // ==================== 回收站 ====================

    /**
     * 获取回收站文件列表
     */
    @GetMapping("/recycle")
    public Result<List<UserFile>> getRecycleBin() {
        Long userId = SecurityContextUtil.currentUserId();
        List<UserFile> files = userFileService.getRecycleBin(userId);
        return Result.success("获取回收站列表成功", files);
    }

    /**
     * 恢复文件
     */
    @PutMapping("/recycle/restore")
    public Result<?> restoreFile(@RequestParam Long fileId) {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.restoreFile(userId, fileId);
        return Result.success("文件恢复成功", null);
    }

    /**
     * 清空回收站
     */
    @DeleteMapping("/recycle/empty")
    public Result<?> emptyRecycleBin() {
        Long userId = SecurityContextUtil.currentUserId();
        userFileService.emptyRecycleBin(userId);
        return Result.success("回收站已清空", null);
    }
}
