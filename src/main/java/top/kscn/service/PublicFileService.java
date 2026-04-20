package top.kscn.service;

import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.PublicFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 公共文件服务接口
 * 基于数据库的公共文件管理系统
 * 使用FileStorageService处理物理文件操作
 */
public interface PublicFileService {
    
    // ==================== 文件上传 ====================
    
    /**
     * 上传公共文件（小文件直接上传，支持秒传）
     * @param parentId 父文件夹ID
     * @param file 上传的文件
     * @param fileMd5 文件MD5
     * @param category 分类
     * @return 公共文件记录
     */
    PublicFile uploadFile(Long parentId, MultipartFile file, String fileMd5, String category);
    
    /**
     * 检查文件是否可以秒传
     * @param fileMd5 文件MD5
     * @param fileSize 文件大小
     * @return 检查结果
     */
    Map<String, Object> checkInstantUpload(String fileMd5, Long fileSize);
    
    /**
     * 从已存在的物理文件创建公共文件记录（秒传使用）
     * @param parentId 父文件夹ID
     * @param fileName 文件名
     * @param storageId 物理文件ID
     * @param category 分类
     * @return 公共文件记录
     */
    PublicFile createFileFromStorage(Long parentId, String fileName, Long storageId, String category);
    
    /**
     * 初始化分片上传
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param fileMd5 文件MD5
     * @return 上传会话信息
     */
    Map<String, Object> initChunkedUpload(String fileName, Long fileSize, String fileMd5);
    
    /**
     * 完成分片上传后创建公共文件记录
     * @param parentId 父文件夹ID
     * @param fileName 文件名
     * @param uploadId 上传会话ID
     * @param category 分类
     * @return 公共文件记录
     */
    PublicFile completeChunkedUpload(Long parentId, String fileName, String uploadId, String category);
    
    // ==================== 文件夹操作 ====================
    
    /**
     * 创建文件夹
     * @param parentId 父文件夹ID
     * @param folderName 文件夹名称
     * @return 文件夹记录
     */
    PublicFile createFolder(Long parentId, String folderName);
    
    /**
     * 获取指定目录下的文件列表
     * @param parentId 父文件夹ID
     * @return 文件列表
     */
    List<PublicFile> listFiles(Long parentId);
    
    /**
     * 获取目录树结构（仅包含文件夹）
     * @return 目录树
     */
    List<Map<String, Object>> getDirectoryTree();
    
    /**
     * 获取所有文件（不包括已删除）
     * @return 文件列表
     */
    List<PublicFile> listAllFiles();
    
    /**
     * 根据分类获取文件
     * @param category 分类
     * @return 文件列表
     */
    List<PublicFile> listFilesByCategory(String category);
    
    /**
     * 搜索文件
     * @param keyword 关键词
     * @return 文件列表
     */
    List<PublicFile> searchFiles(String keyword);
    
    /**
     * 根据ID获取文件
     * @param fileId 文件ID
     * @return 文件记录
     */
    PublicFile getFileById(Long fileId);
    
    // ==================== 文件操作 ====================
    
    /**
     * 删除文件（逻辑删除，移入回收站）
     * @param fileId 文件ID
     */
    void deleteFile(Long fileId);
    
    /**
     * 彻底删除文件（物理删除）
     * @param fileId 文件ID
     */
    void permanentDeleteFile(Long fileId);
    
    /**
     * 重命名文件
     * @param fileId 文件ID
     * @param newName 新名称
     */
    void renameFile(Long fileId, String newName);
    
    /**
     * 移动文件
     * @param fileId 文件ID
     * @param targetParentId 目标父文件夹ID
     */
    void moveFile(Long fileId, Long targetParentId);
    
    // ==================== 文件下载 ====================
    
    /**
     * 下载文件（流式下载，支持断点续传）
     * @param fileId 文件ID
     * @param range Range请求头
     * @param response HTTP响应
     */
    void downloadFile(Long fileId, String range, HttpServletResponse response) throws IOException;
    
    /**
     * 下载文件夹（打包成ZIP）
     * @param folderId 文件夹ID
     * @param range Range请求头
     * @param response HTTP响应
     */
    void downloadFolder(Long folderId, String range, HttpServletResponse response) throws IOException;
    
    /**
     * 创建批量下载任务
     * @param fileIds 文件ID列表
     * @return 任务信息
     */
    Map<String, Object> createBatchDownloadTask(List<Long> fileIds);
    
    /**
     * 下载批量下载任务的ZIP文件
     * @param taskId 任务ID
     * @param range Range请求头
     * @param response HTTP响应
     */
    void downloadBatchTask(String taskId, String range, HttpServletResponse response) throws IOException;
    
    // ==================== 回收站 ====================
    
    /**
     * 获取回收站文件列表
     * @return 文件列表
     */
    List<PublicFile> getRecycleBin();
    
    /**
     * 恢复文件
     * @param fileId 文件ID
     */
    void restoreFile(Long fileId);
    
    /**
     * 清空回收站
     */
    void emptyRecycleBin();
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取统计信息
     * @return 统计信息
     */
    Map<String, Object> getStatistics();

    long countPublicResourceFiles();
}
