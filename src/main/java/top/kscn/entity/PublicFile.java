package top.kscn.entity;

import java.time.LocalDateTime;

/**
 * 公共文件系统表
 * 存储公共文件/文件夹逻辑结构（与user_file结构统一）
 */
public class PublicFile {
    private Long fileId;             // 文件ID
    private Long parentId;           // 父文件夹ID（NULL为根目录）
    private String fileName;         // 文件名
    private Integer isFolder;        // 是否文件夹（1-是 0-否）
    private Long storageId;          // 关联物理文件ID（文件才有）
    private Long fileSize;           // 文件大小
    private String fileType;         // 文件类型后缀
    private String category;         // 分类
    private Integer downloadCount;   // 下载次数
    private Integer isDeleted;       // 是否删除
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    
    // 非数据库字段
    private Integer itemCount;       // 文件夹内项目数量（仅用于前端显示）

    public PublicFile() {
    }

    // 创建文件夹的构造函数
    public PublicFile(Long parentId, String fileName) {
        this.parentId = parentId;
        this.fileName = fileName;
        this.isFolder = 1;
        this.fileSize = 0L;
        this.downloadCount = 0;
        this.isDeleted = 0;
    }

    // 创建文件的构造函数
    public PublicFile(Long parentId, String fileName, Long storageId, Long fileSize, String fileType, String category) {
        this.parentId = parentId;
        this.fileName = fileName;
        this.isFolder = 0;
        this.storageId = storageId;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.category = category;
        this.downloadCount = 0;
        this.isDeleted = 0;
    }

    // Getters and Setters
    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getIsFolder() {
        return isFolder;
    }

    public void setIsFolder(Integer isFolder) {
        this.isFolder = isFolder;
    }

    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(Long storageId) {
        this.storageId = storageId;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
}

