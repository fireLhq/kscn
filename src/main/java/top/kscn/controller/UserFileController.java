package top.kscn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.common.Result;
import top.kscn.entity.UserFile;
import top.kscn.service.FileStorageService;
import top.kscn.service.UserFileService;
import top.kscn.utils.SecurityInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用户文件管理控制器
 * 基于数据库的用户文件管理系统
 * userId自动从token中解析
 */
@RestController
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

        Long userId = SecurityInfo.getUserId();
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

        Long userId = SecurityInfo.getUserId();
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

        Long userId = SecurityInfo.getUserId();
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
        return Result.success("分片上传成功");
    }

    /**
     * 完成分片上传
     */
    @PostMapping("/upload/complete")
    public Result<UserFile> completeChunkedUpload(
            @RequestParam String uploadId,
            @RequestParam(required = false) Long parentId,
            @RequestParam String fileName) {

        Long userId = SecurityInfo.getUserId();
        UserFile userFile = userFileService.completeChunkedUpload(userId, parentId, fileName, uploadId);
        return Result.success("上传完成", userFile);
    }

    /**
     * 取消分片上传
     */
    @PostMapping("/upload/cancel")
    public Result<?> cancelChunkedUpload(@RequestParam String uploadId) {
        fileStorageService.cancelChunkedUpload(uploadId);
        return Result.success("取消成功");
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

        Long userId = SecurityInfo.getUserId();
        UserFile folder = userFileService.createFolder(userId, parentId, folderName);
        return Result.success("文件夹创建成功", folder);
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public Result<List<UserFile>> listFiles(
            @RequestParam(required = false) Long parentId) {

        Long userId = SecurityInfo.getUserId();
        List<UserFile> files = userFileService.listFiles(userId, parentId);
        return Result.success("获取文件列表成功", files);
    }

    /**
     * 获取目录树结构（仅包含文件夹）
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getDirectoryTree() {
        Long userId = SecurityInfo.getUserId();
        List<Map<String, Object>> tree = userFileService.getDirectoryTree(userId);
        return Result.success("获取目录树成功", tree);
    }

    /**
     * 根据路径获取文件夹ID
     */
    @GetMapping("/folder/id")
    public Result<Long> getFolderIdByPath(@RequestParam String path) {

        Long userId = SecurityInfo.getUserId();
        Long folderId = userFileService.getFolderIdByPath(userId, path);
        return Result.success("获取文件夹ID成功", folderId);
    }

    // ==================== 文件操作 ====================

    /**
     * 删除文件（移入回收站）
     */
    @DeleteMapping("/delete")
    public Result<?> deleteFile(@RequestParam Long fileId) {

        Long userId = SecurityInfo.getUserId();
        userFileService.deleteFile(userId, fileId);
        return Result.success("文件已移入回收站");
    }

    /**
     * 彻底删除文件
     */
    @DeleteMapping("/delete/permanent")
    public Result<?> permanentDeleteFile(@RequestParam Long fileId) {

        Long userId = SecurityInfo.getUserId();
        userFileService.permanentDeleteFile(userId, fileId);
        return Result.success("文件已彻底删除");
    }

    /**
     * 重命名文件
     */
    @PutMapping("/rename")
    public Result<?> renameFile(
            @RequestParam Long fileId,
            @RequestParam String newName) {

        Long userId = SecurityInfo.getUserId();
        userFileService.renameFile(userId, fileId, newName);
        return Result.success("重命名成功");
    }

    /**
     * 移动文件
     */
    @PutMapping("/move")
    public Result<?> moveFile(
            @RequestParam Long fileId,
            @RequestParam(required = false) Long targetParentId) {

        Long userId = SecurityInfo.getUserId();
        userFileService.moveFile(userId, fileId, targetParentId);
        return Result.success("移动成功");
    }

    /**
     * 搜索文件
     */
    @GetMapping("/search")
    public Result<List<UserFile>> searchFiles(@RequestParam String keyword) {

        Long userId = SecurityInfo.getUserId();
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

        Long userId = SecurityInfo.getUserId();
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

        Long userId = SecurityInfo.getUserId();
        userFileService.downloadFolder(userId, folderId, range, response);
    }

    /**
     * 批量下载文件（打包成ZIP）
     */
    @PostMapping("/download/batch")
    public Result<Map<String, Object>> batchDownload(@RequestBody List<Long> fileIds) {
        Long userId = SecurityInfo.getUserId();
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

        Long userId = SecurityInfo.getUserId();
        userFileService.downloadBatchTask(userId, taskId, range, response);
    }

    // ==================== 空间管理 ====================

    /**
     * 获取用户空间使用情况
     */
    @GetMapping("/space")
    public Result<Map<String, Object>> getUserSpaceInfo() {
        Long userId = SecurityInfo.getUserId();
        Map<String, Object> spaceInfo = userFileService.getUserSpaceInfo(userId);
        return Result.success("获取空间信息成功", spaceInfo);
    }

    // ==================== 回收站 ====================

    /**
     * 获取回收站文件列表
     */
    @GetMapping("/recycle")
    public Result<List<UserFile>> getRecycleBin() {
        Long userId = SecurityInfo.getUserId();
        List<UserFile> files = userFileService.getRecycleBin(userId);
        return Result.success("获取回收站列表成功", files);
    }

    /**
     * 恢复文件
     */
    @PutMapping("/recycle/restore")
    public Result<?> restoreFile(@RequestParam Long fileId) {

        Long userId = SecurityInfo.getUserId();
        userFileService.restoreFile(userId, fileId);
        return Result.success("文件恢复成功");
    }

    /**
     * 清空回收站
     */
    @DeleteMapping("/recycle/empty")
    public Result<?> emptyRecycleBin() {
        Long userId = SecurityInfo.getUserId();
        userFileService.emptyRecycleBin(userId);
        return Result.success("回收站已清空");
    }
}


