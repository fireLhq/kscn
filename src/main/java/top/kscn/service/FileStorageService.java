package top.kscn.service;

import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.FileStorage;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 文件物理存储服务接口
 * 统一处理用户文件和公共文件的物理存储操作
 * 支持：MD5去重、秒传、分片上传、流式下载、断点续传
 */
public interface FileStorageService {
    
    // ==================== MD5去重和秒传 ====================
    
    /**
     * 根据MD5查询物理文件
     * @param fileMd5 文件MD5
     * @return 物理文件记录，不存在返回null
     */
    FileStorage getByMd5(String fileMd5);
    
    /**
     * 检查文件是否可以秒传
     * @param fileMd5 文件MD5
     * @param fileSize 文件大小
     * @return 检查结果
     */
    Map<String, Object> checkInstantUpload(String fileMd5, Long fileSize);
    
    /**
     * 增加物理文件引用计数
     * @param storageId 物理文件ID
     * @return 新的引用计数
     */
    int incrementRefCount(Long storageId);
    
    /**
     * 减少物理文件引用计数（引用为0时删除物理文件）
     * @param storageId 物理文件ID
     * @return 新的引用计数（-1表示已删除）
     */
    int decrementRefCount(Long storageId);
    
    // ==================== 文件上传 ====================
    
    /**
     * 上传小文件（直接上传）
     * @param file 文件
     * @param fileMd5 文件MD5
     * @return 物理文件记录
     */
    FileStorage uploadFile(MultipartFile file, String fileMd5);
    
    /**
     * 初始化分片上传
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param fileMd5 文件MD5
     * @return 上传会话信息
     */
    Map<String, Object> initChunkedUpload(String fileName, Long fileSize, String fileMd5);
    
    /**
     * 上传分片
     * @param uploadId 上传会话ID
     * @param chunkNumber 分片编号
     * @param chunk 分片文件
     */
    void uploadChunk(String uploadId, Integer chunkNumber, MultipartFile chunk);
    
    /**
     * 完成分片上传
     * @param uploadId 上传会话ID
     * @return 物理文件记录
     */
    FileStorage completeChunkedUpload(String uploadId);
    
    /**
     * 取消分片上传
     * @param uploadId 上传会话ID
     */
    void cancelChunkedUpload(String uploadId);
    
    /**
     * 获取上传进度
     * @param uploadId 上传会话ID
     * @return 进度信息
     */
    Map<String, Object> getUploadProgress(String uploadId);
    
    // ==================== 文件下载 ====================
    
    /**
     * 流式下载文件（支持断点续传）
     * @param storageId 物理文件ID
     * @param fileName 下载文件名
     * @param range Range请求头
     * @param response HTTP响应
     */
    void downloadFile(Long storageId, String fileName, String range, HttpServletResponse response) throws IOException;
    
    /**
     * 根据storageId获取物理文件记录
     * @param storageId 物理文件ID
     * @return 物理文件记录
     */
    FileStorage getById(Long storageId);
    
    // ==================== 定时清理 ====================
    
    /**
     * 清理临时文件（分片上传的临时文件）
     */
    void cleanupTempFiles();
}


