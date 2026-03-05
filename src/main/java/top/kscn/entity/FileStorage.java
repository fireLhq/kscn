package top.kscn.entity;

import java.time.LocalDateTime;

/**
 * 物理文件存储表
 * 存储真实的物理文件信息，通过MD5去重
 */
public class FileStorage {
    private Long storageId;          // 物理文件ID
    private String fileMd5;          // 文件MD5唯一标识
    private Long fileSize;           // 文件大小（字节）
    private String filePath;         // 磁盘真实路径（相对于storage根目录）
    private Integer refCount;        // 引用次数
    private Integer status;          // 状态（1-正常 0-已删除）
    private LocalDateTime createTime; // 创建时间

    public FileStorage() {
    }

    public FileStorage(String fileMd5, Long fileSize, String filePath) {
        this.fileMd5 = fileMd5;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.refCount = 1;
        this.status = 1;
    }

    // Getters and Setters
    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(Long storageId) {
        this.storageId = storageId;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getRefCount() {
        return refCount;
    }

    public void setRefCount(Integer refCount) {
        this.refCount = refCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

