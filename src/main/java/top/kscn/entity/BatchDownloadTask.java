package top.kscn.entity;

import java.util.List;

/**
 * 批量下载任务
 */
public class BatchDownloadTask {
    private String taskId;
    private List<String> paths;
    private String status; // pending, processing, completed, failed
    private String zipFilePath;
    private long createTime;
    private long completeTime;
    private String errorMessage;
    private long fileSize;

    public BatchDownloadTask() {
    }

    public BatchDownloadTask(String taskId, List<String> paths) {
        this.taskId = taskId;
        this.paths = paths;
        this.status = "pending";
        this.createTime = System.currentTimeMillis();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getZipFilePath() {
        return zipFilePath;
    }

    public void setZipFilePath(String zipFilePath) {
        this.zipFilePath = zipFilePath;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}

