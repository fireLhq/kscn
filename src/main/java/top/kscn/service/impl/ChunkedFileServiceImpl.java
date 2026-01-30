package top.kscn.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.service.ChunkedFileService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChunkedFileServiceImpl implements ChunkedFileService {

    @Value("${file.root-path}")
    private String rootPath;

    // 分片大小：1MB
    private static final int CHUNK_SIZE = 1024 * 1024;
    
    // 存储上传会话信息
    private final Map<String, UploadSession> uploadSessions = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> initChunkedUpload(String fileName, Long fileSize, String fileMd5, String path) {
        String uploadId = UUID.randomUUID().toString();
        int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
        
        UploadSession session = new UploadSession();
        session.setUploadId(uploadId);
        session.setFileName(fileName);
        session.setFileSize(fileSize);
        session.setFileMd5(fileMd5);
        session.setPath(path);
        session.setTotalChunks(totalChunks);
        session.setUploadedChunks(new HashSet<>());
        session.setStartTime(System.currentTimeMillis());
        
        uploadSessions.put(uploadId, session);
        
        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", uploadId);
        result.put("chunkSize", CHUNK_SIZE);
        result.put("totalChunks", totalChunks);
        
        return result;
    }

    @Override
    public void uploadChunk(String uploadId, Integer chunkNumber, MultipartFile chunk) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session == null) {
            throw new RuntimeException("上传会话不存在");
        }

        try {
            // 创建临时目录
            Path tempDir = Paths.get(rootPath, ".temp", uploadId);
            Files.createDirectories(tempDir);
            
            // 保存分片文件
            Path chunkFile = tempDir.resolve("chunk_" + chunkNumber);
            chunk.transferTo(chunkFile.toFile());
            
            // 记录已上传的分片
            session.getUploadedChunks().add(chunkNumber);
            
        } catch (IOException e) {
            throw new RuntimeException("分片上传失败: " + e.getMessage());
        }
    }

    @Override
    public String completeChunkedUpload(String uploadId) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session == null) {
            throw new RuntimeException("上传会话不存在");
        }

        try {
            // 检查所有分片是否已上传
            if (session.getUploadedChunks().size() != session.getTotalChunks()) {
                throw new RuntimeException("分片上传不完整");
            }

            // 合并分片
            Path tempDir = Paths.get(rootPath, ".temp", uploadId);
            Path finalPath = Paths.get(rootPath, session.getPath().substring(1), session.getFileName());
            Files.createDirectories(finalPath.getParent());
            
            try (FileOutputStream fos = new FileOutputStream(finalPath.toFile())) {
                for (int i = 0; i < session.getTotalChunks(); i++) {
                    Path chunkFile = tempDir.resolve("chunk_" + i);
                    if (Files.exists(chunkFile)) {
                        Files.copy(chunkFile, fos);
                    }
                }
            }

            // 验证文件MD5
            String actualMd5 = calculateMD5(finalPath.toFile());
            if (!actualMd5.equals(session.getFileMd5())) {
                Files.deleteIfExists(finalPath);
                throw new RuntimeException("文件校验失败");
            }

            // 清理临时文件
            deleteDirectory(tempDir.toFile());
            
            // 移除上传会话
            uploadSessions.remove(uploadId);
            
            return finalPath.toString();
            
        } catch (IOException e) {
            throw new RuntimeException("合并文件失败: " + e.getMessage());
        }
    }

    @Override
    public void cancelChunkedUpload(String uploadId) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session != null) {
            try {
                // 清理临时文件
                Path tempDir = Paths.get(rootPath, ".temp", uploadId);
                deleteDirectory(tempDir.toFile());
            } catch (Exception e) {
                // 记录日志
            }
            uploadSessions.remove(uploadId);
        }
    }

    @Override
    public Map<String, Object> getUploadProgress(String uploadId) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session == null) {
            throw new RuntimeException("上传会话不存在");
        }

        Map<String, Object> progress = new HashMap<>();
        progress.put("uploadedChunks", session.getUploadedChunks().size());
        progress.put("totalChunks", session.getTotalChunks());
        progress.put("progress", (double) session.getUploadedChunks().size() / session.getTotalChunks() * 100);
        progress.put("fileName", session.getFileName());
        progress.put("fileSize", session.getFileSize());
        
        return progress;
    }

    @Override
    public Map<String, Object> getFileInfo(String path) {
        try {
            // 使用安全路径解析
            Path filePath = resolveSafePath(path);
            File file = filePath.toFile();
            
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", file.getName());
            fileInfo.put("fileSize", file.length());
            fileInfo.put("exists", file.exists());
            
            if (!file.exists()) {
                // 文件不存在时返回基本信息，不抛出异常
                fileInfo.put("error", "文件不存在: " + path);
            }
            
            return fileInfo;
        } catch (Exception e) {
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("exists", false);
            fileInfo.put("error", "获取文件信息失败: " + e.getMessage());
            return fileInfo;
        }
    }

    private String calculateMD5(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算MD5失败: " + e.getMessage());
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    /**
     * 安全解析路径
     * - 支持 "/" 映射为 root
     * - 兼容 Windows/Linux
     * - 防止路径遍历攻击
     */
    @Override
    public Path resolveSafePath(String relativePath) {
        // 验证路径格式
        validatePathFormat(relativePath);

        // 根目录特殊处理
        if ("/".equals(relativePath)) {
            relativePath = "";
        } else {
            // 移除开头的斜杠
            relativePath = relativePath.substring(1);
        }

        Path root = Paths.get(rootPath).toAbsolutePath().normalize();
        Path full = root.resolve(relativePath).normalize();

        // 确保解析后的路径仍在根目录内
        if (!full.startsWith(root)) {
            throw new RuntimeException("非法路径访问: " + relativePath);
        }
        return full;
    }

    /**
     * 验证路径格式
     * - 必须以/开头
     * - 不能包含/../
     * - 不能包含空路径段
     */
    private void validatePathFormat(String relativePath) {
        if (relativePath == null) {
            throw new RuntimeException("路径不能为空");
        }

        // 必须以/开头
        if (!relativePath.startsWith("/")) {
            throw new RuntimeException("路径必须以/开头: " + relativePath);
        }

        // 不能包含/../
        if (relativePath.contains("/../")) {
            throw new RuntimeException("路径不能包含/../: " + relativePath);
        }

        // 不能包含空路径段 (如 //)
        if (relativePath.contains("//")) {
            throw new RuntimeException("路径不能包含空路径段: " + relativePath);
        }

        // 根目录特殊情况处理
        if (!"/".equals(relativePath)) {
            // 不能以/结尾 (除了根目录)
            if (relativePath.endsWith("/")) {
                throw new RuntimeException("路径不能以/结尾: " + relativePath);
            }
        }
    }

    // 上传会话内部类
    private static class UploadSession {
        private String uploadId;
        private String fileName;
        private Long fileSize;
        private String fileMd5;
        private String path;
        private int totalChunks;
        private Set<Integer> uploadedChunks;
        private long startTime;

        // Getters and Setters
        public String getUploadId() { return uploadId; }
        public void setUploadId(String uploadId) { this.uploadId = uploadId; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getFileMd5() { return fileMd5; }
        public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public int getTotalChunks() { return totalChunks; }
        public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }
        
        public Set<Integer> getUploadedChunks() { return uploadedChunks; }
        public void setUploadedChunks(Set<Integer> uploadedChunks) { this.uploadedChunks = uploadedChunks; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
    }
}
