package top.kscn.service;

import java.util.Map;

public interface ChunkedFileService {
    
    /**
     * 初始化分片上传
     */
    Map<String, Object> initChunkedUpload(String fileName, Long fileSize, String fileMd5, String path);
    
    /**
     * 上传分片
     */
    void uploadChunk(String uploadId, Integer chunkNumber, org.springframework.web.multipart.MultipartFile chunk);
    
    /**
     * 完成分片上传
     */
    String completeChunkedUpload(String uploadId);
    
    /**
     * 取消分片上传
     */
    void cancelChunkedUpload(String uploadId);
    
    /**
     * 获取上传进度
     */
    Map<String, Object> getUploadProgress(String uploadId);
    
    /**
     * 获取文件信息
     */
    Map<String, Object> getFileInfo(String path);
    
    /**
     * 安全解析路径
     */
    java.nio.file.Path resolveSafePath(String relativePath);
}
