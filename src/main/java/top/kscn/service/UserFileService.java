package top.kscn.service;

import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.UserFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用户文件服务接口
 * 基于数据库的用户文件管理系统
 * 使用FileStorageService处理物理文件操作
 */
public interface UserFileService {
    
    // ==================== 文件上传 ====================
    
    /**
     * 上传文件（小文件直接上传，支持秒传）
     * @param userId 用户ID
     * @param parentId 父文件夹ID（null为根目录）
     * @param file 上传的文件
     * @param fileMd5 文件MD5
     * @return 用户文件记录
     */
    UserFile uploadFile(Long userId, Long parentId, MultipartFile file, String fileMd5);
    
    /**
     * 检查文件是否可以秒传
     * @param userId 用户ID
     * @param fileMd5 文件MD5
     * @param fileSize 文件大小
     * @return 检查结果（包含canInstant, storageId等）
     */
    Map<String, Object> checkInstantUpload(Long userId, String fileMd5, Long fileSize);
    
    /**
     * 从已存在的物理文件创建用户文件记录（秒传使用）
     * @param userId 用户ID
     * @param parentId 父文件夹ID
     * @param fileName 文件名
     * @param storageId 物理文件ID
     * @return 用户文件记录
     */
    UserFile createFileFromStorage(Long userId, Long parentId, String fileName, Long storageId);
    
    /**
     * 初始化分片上传
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param fileMd5 文件MD5
     * @return 上传会话信息
     */
    Map<String, Object> initChunkedUpload(String fileName, Long fileSize, String fileMd5);
    
    /**
     * 完成分片上传后创建用户文件记录
     * @param userId 用户ID
     * @param parentId 父文件夹ID
     * @param fileName 文件名
     * @param uploadId 上传会话ID
     * @return 用户文件记录
     */
    UserFile completeChunkedUpload(Long userId, Long parentId, String fileName, String uploadId);
    
    // ==================== 文件夹操作 ====================
    
    /**
     * 创建文件夹
     * @param userId 用户ID
     * @param parentId 父文件夹ID
     * @param folderName 文件夹名称
     * @return 文件夹记录
     */
    UserFile createFolder(Long userId, Long parentId, String folderName);
    
    /**
     * 获取用户指定目录下的文件列表
     * @param userId 用户ID
     * @param parentId 父文件夹ID
     * @return 文件列表
     */
    List<UserFile> listFiles(Long userId, Long parentId);
    
    /**
     * 获取目录树结构（仅包含文件夹）
     * @param userId 用户ID
     * @return 目录树
     */
    List<Map<String, Object>> getDirectoryTree(Long userId);
    
    /**
     * 根据路径获取文件夹ID
     * @param userId 用户ID
     * @param path 路径（如 /folder1/folder2）
     * @return 文件夹ID
     */
    Long getFolderIdByPath(Long userId, String path);
    
    // ==================== 文件操作 ====================
    
    /**
     * 删除文件（逻辑删除，移入回收站）
     * @param userId 用户ID
     * @param fileId 文件ID
     */
    void deleteFile(Long userId, Long fileId);
    
    /**
     * 彻底删除文件（物理删除）
     * @param userId 用户ID
     * @param fileId 文件ID
     */
    void permanentDeleteFile(Long userId, Long fileId);
    
    /**
     * 重命名文件
     * @param userId 用户ID
     * @param fileId 文件ID
     * @param newName 新名称
     */
    void renameFile(Long userId, Long fileId, String newName);
    
    /**
     * 移动文件
     * @param userId 用户ID
     * @param fileId 文件ID
     * @param targetParentId 目标父文件夹ID
     */
    void moveFile(Long userId, Long fileId, Long targetParentId);
    
    /**
     * 搜索文件
     * @param userId 用户ID
     * @param keyword 关键词
     * @return 文件列表
     */
    List<UserFile> searchFiles(Long userId, String keyword);
    
    // ==================== 文件下载 ====================
    
    /**
     * 下载文件（流式下载，支持断点续传）
     * @param userId 用户ID
     * @param fileId 文件ID
     * @param range Range请求头
     * @param response HTTP响应
     */
    void downloadFile(Long userId, Long fileId, String range, HttpServletResponse response) throws IOException;
    
    /**
     * 下载文件夹（打包成ZIP）
     * @param userId 用户ID
     * @param folderId 文件夹ID
     * @param range Range请求头
     * @param response HTTP响应
     */
    void downloadFolder(Long userId, Long folderId, String range, HttpServletResponse response) throws IOException;
    
    /**
     * 创建批量下载任务
     * @param userId 用户ID
     * @param fileIds 文件ID列表
     * @return 任务信息
     */
    Map<String, Object> createBatchDownloadTask(Long userId, List<Long> fileIds);
    
    /**
     * 下载批量下载任务的ZIP文件
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param range Range请求头
     * @param response HTTP响应
     */
    void downloadBatchTask(Long userId, String taskId, String range, HttpServletResponse response) throws IOException;
    
    // ==================== 空间管理 ====================
    
    /**
     * 获取用户空间使用情况
     * @param userId 用户ID
     * @return 空间信息（totalSpace, usedSpace, availableSpace）
     */
    Map<String, Object> getUserSpaceInfo(Long userId);
    
    /**
     * 检查用户空间是否足够
     * @param userId 用户ID
     * @param fileSize 文件大小
     * @return 是否足够
     */
    boolean checkUserSpace(Long userId, Long fileSize);

    /**
     * 获取用户文件数量（不包含文件夹和已删除）
     * @param userId 用户ID
     * @return 文件数量
     */
    Long getUserFileCount(Long userId);

    /**
     * 重新计算并持久化用户已用空间
     * @param userId 用户ID
     * @return 重新计算后的已用空间
     */
    Long recalculateUsedSpace(Long userId);
    
    // ==================== 回收站 ====================
    
    /**
     * 获取回收站文件列表
     * @param userId 用户ID
     * @return 文件列表
     */
    List<UserFile> getRecycleBin(Long userId);
    
    /**
     * 恢复文件
     * @param userId 用户ID
     * @param fileId 文件ID
     */
    void restoreFile(Long userId, Long fileId);
    
    /**
     * 清空回收站
     * @param userId 用户ID
     */
    void emptyRecycleBin(Long userId);
}


