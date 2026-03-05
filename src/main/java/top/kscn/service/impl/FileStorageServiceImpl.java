package top.kscn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.FileStorage;
import top.kscn.exception.CustomException;
import top.kscn.mapper.FileStorageMapper;
import top.kscn.service.FileStorageService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * 文件物理存储服务实现
 * 统一处理用户文件和公共文件的物理存储操作
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private FileStorageMapper fileStorageMapper;

    @Value("${file.storage-root}")
    private String storageRoot;

    // 分片大小：1MB
    private static final int CHUNK_SIZE = 1024 * 1024;
    
    // 下载缓冲区：8KB
    private static final int BUFFER_SIZE = 8192;
    
    // 单文件分片并发限制：3
    private static final int SINGLE_FILE_CONCURRENT_LIMIT = 3;
    
    // 全局上传并发限制：10
    private static final Semaphore GLOBAL_UPLOAD_SEMAPHORE = new Semaphore(10);
    
    // 存储上传会话信息
    private final Map<String, UploadSession> uploadSessions = new ConcurrentHashMap<>();
    
    // 存储每个上传会话的并发控制信号量
    private final Map<String, Semaphore> uploadSemaphores = new ConcurrentHashMap<>();

    // ==================== MD5去重和秒传 ====================

    @Override
    public FileStorage getByMd5(String fileMd5) {
        return fileStorageMapper.selectByMd5(fileMd5);
    }

    @Override
    public Map<String, Object> checkInstantUpload(String fileMd5, Long fileSize) {
        Map<String, Object> result = new HashMap<>();
        FileStorage storage = fileStorageMapper.selectByMd5(fileMd5);
        
        if (storage != null && storage.getStatus() == 1) {
            result.put("canInstant", true);
            result.put("storageId", storage.getStorageId());
            result.put("message", "文件已存在，可以秒传");
        } else {
            result.put("canInstant", false);
            result.put("message", "需要上传文件");
        }
        
        return result;
    }

    @Override
    @Transactional
    public int incrementRefCount(Long storageId) {
        FileStorage storage = fileStorageMapper.selectById(storageId);
        if (storage == null) {
            throw new CustomException(404, "物理文件不存在");
        }
        
        fileStorageMapper.incrementRefCount(storageId);
        return storage.getRefCount() + 1;
    }

    @Override
    @Transactional
    public int decrementRefCount(Long storageId) {
        FileStorage storage = fileStorageMapper.selectById(storageId);
        if (storage == null) {
            return -1;
        }
        
        int newRefCount = storage.getRefCount() - 1;
        
        if (newRefCount <= 0) {
            // 引用计数为0，删除物理文件
            try {
                Files.deleteIfExists(Paths.get(storage.getFilePath()));
            } catch (IOException e) {
                // 记录日志但不抛出异常
                System.err.println("删除物理文件失败: " + storage.getFilePath());
            }
            fileStorageMapper.deleteIfNoRef(storageId);
            return -1;
        } else {
            // 更新引用计数
            fileStorageMapper.decrementRefCount(storageId);
            return newRefCount;
        }
    }

    // ==================== 文件上传 ====================

    @Override
    @Transactional
    public FileStorage uploadFile(MultipartFile file, String fileMd5) {
        // 检查是否已存在
        FileStorage existing = fileStorageMapper.selectByMd5(fileMd5);
        if (existing != null && existing.getStatus() == 1) {
            // 秒传：引用计数+1
            incrementRefCount(existing.getStorageId());
            return existing;
        }
        
        // 保存物理文件
        String filePath = savePhysicalFile(file, fileMd5);
        
        // 创建物理文件记录
        FileStorage storage = new FileStorage();
        storage.setFileMd5(fileMd5);
        storage.setFileSize(file.getSize());
        storage.setFilePath(filePath);
        storage.setRefCount(1);
        storage.setStatus(1);
        
        fileStorageMapper.insert(storage);
        return storage;
    }

    @Override
    public Map<String, Object> initChunkedUpload(String fileName, Long fileSize, String fileMd5) {
        // 验证文件大小（最大2GB）
        if (fileSize > 2L * 1024 * 1024 * 1024) {
            throw new CustomException(400, "文件大小超过限制（最大2GB）");
        }
        
        String uploadId = UUID.randomUUID().toString();
        int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
        
        UploadSession session = new UploadSession();
        session.setUploadId(uploadId);
        session.setFileName(fileName);
        session.setFileSize(fileSize);
        session.setFileMd5(fileMd5);
        session.setTotalChunks(totalChunks);
        session.setUploadedChunks(new HashSet<>());
        session.setStartTime(System.currentTimeMillis());
        
        uploadSessions.put(uploadId, session);
        uploadSemaphores.put(uploadId, new Semaphore(SINGLE_FILE_CONCURRENT_LIMIT));
        
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
            throw new CustomException(400, "上传会话不存在");
        }
        
        Semaphore uploadSemaphore = uploadSemaphores.get(uploadId);
        if (uploadSemaphore == null) {
            throw new CustomException(400, "上传会话信号量不存在");
        }

        try {
            // 获取全局上传许可
            GLOBAL_UPLOAD_SEMAPHORE.acquire();
            
            try {
                // 获取单文件上传许可
                uploadSemaphore.acquire();
                
                try {
                    // 创建临时目录（使用MD5作为目录名）
                    Path tempDir = Paths.get(storageRoot, ".upload-temp", session.getFileMd5());
                    Files.createDirectories(tempDir);
                    
                    // 保存分片文件（流式写入）
                    Path chunkFile = tempDir.resolve("chunk-" + chunkNumber);
                    
                    try (InputStream inputStream = chunk.getInputStream();
                         OutputStream outputStream = new BufferedOutputStream(
                                 new FileOutputStream(chunkFile.toFile()), BUFFER_SIZE)) {
                        
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    // 记录已上传的分片
                    session.getUploadedChunks().add(chunkNumber);
                    
                } finally {
                    uploadSemaphore.release();
                }
            } finally {
                GLOBAL_UPLOAD_SEMAPHORE.release();
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(500, "上传被中断");
        } catch (IOException e) {
            throw new CustomException(500, "分片上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FileStorage completeChunkedUpload(String uploadId) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session == null) {
            throw new CustomException(400, "上传会话不存在");
        }

        try {
            // 检查所有分片是否已上传
            if (session.getUploadedChunks().size() != session.getTotalChunks()) {
                throw new CustomException(400, "分片上传不完整，已上传: " + 
                    session.getUploadedChunks().size() + "/" + session.getTotalChunks());
            }

            // 检查是否已存在（可能在上传过程中其他用户已上传）
            FileStorage existing = fileStorageMapper.selectByMd5(session.getFileMd5());
            if (existing != null && existing.getStatus() == 1) {
                // 清理临时文件
                Path tempDir = Paths.get(storageRoot, ".upload-temp", session.getFileMd5());
                deleteDirectory(tempDir.toFile());
                
                // 秒传：引用计数+1
                incrementRefCount(existing.getStorageId());
                
                // 移除上传会话
                uploadSessions.remove(uploadId);
                uploadSemaphores.remove(uploadId);
                
                return existing;
            }

            // 合并分片到最终文件
            String finalPath = mergeChunks(session);
            
            // 创建物理文件记录
            FileStorage storage = new FileStorage();
            storage.setFileMd5(session.getFileMd5());
            storage.setFileSize(session.getFileSize());
            storage.setFilePath(finalPath);
            storage.setRefCount(1);
            storage.setStatus(1);
            
            fileStorageMapper.insert(storage);
            
            // 清理临时文件
            Path tempDir = Paths.get(storageRoot, ".upload-temp", session.getFileMd5());
            deleteDirectory(tempDir.toFile());
            
            // 移除上传会话
            uploadSessions.remove(uploadId);
            uploadSemaphores.remove(uploadId);
            
            return storage;
            
        } catch (IOException e) {
            throw new CustomException(500, "合并文件失败: " + e.getMessage());
        }
    }

    @Override
    public void cancelChunkedUpload(String uploadId) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session != null) {
            try {
                // 清理临时文件
                Path tempDir = Paths.get(storageRoot, ".upload-temp", session.getFileMd5());
                deleteDirectory(tempDir.toFile());
            } catch (Exception e) {
                // 记录日志
            }
            uploadSessions.remove(uploadId);
            uploadSemaphores.remove(uploadId);
        }
    }

    @Override
    public Map<String, Object> getUploadProgress(String uploadId) {
        UploadSession session = uploadSessions.get(uploadId);
        if (session == null) {
            throw new CustomException(400, "上传会话不存在");
        }

        Map<String, Object> progress = new HashMap<>();
        progress.put("uploadedChunks", session.getUploadedChunks().size());
        progress.put("totalChunks", session.getTotalChunks());
        progress.put("progress", (double) session.getUploadedChunks().size() / session.getTotalChunks() * 100);
        progress.put("fileName", session.getFileName());
        progress.put("fileSize", session.getFileSize());
        
        return progress;
    }

    // ==================== 文件下载 ====================

    @Override
    public void downloadFile(Long storageId, String fileName, String range, HttpServletResponse response) throws IOException {
        FileStorage storage = fileStorageMapper.selectById(storageId);
        if (storage == null || storage.getStatus() != 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        File physicalFile = new File(storage.getFilePath());
        if (!physicalFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "物理文件不存在");
            return;
        }

        long fileLength = physicalFile.length();
        long start = 0;
        long end = fileLength - 1;

        // 处理Range请求（断点续传）
        if (range != null && range.startsWith("bytes=")) {
            String[] ranges = range.substring(6).split("-");
            try {
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }
        }

        // 验证范围
        if (start > end || start < 0 || end >= fileLength) {
            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }

        long contentLength = end - start + 1;

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Accept-Ranges", "bytes");

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);

        if (range != null) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        response.setHeader("Content-Length", String.valueOf(contentLength));

        // 流式读取并写入响应
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(physicalFile, "r");
             OutputStream outputStream = response.getOutputStream()) {

            randomAccessFile.seek(start);

            byte[] buffer = new byte[BUFFER_SIZE];
            long bytesToRead = contentLength;

            while (bytesToRead > 0) {
                int readSize = (int) Math.min(buffer.length, bytesToRead);
                int bytesRead = randomAccessFile.read(buffer, 0, readSize);

                if (bytesRead == -1) {
                    break;
                }

                outputStream.write(buffer, 0, bytesRead);
                bytesToRead -= bytesRead;
            }

            outputStream.flush();
        }
    }

    @Override
    public FileStorage getById(Long storageId) {
        return fileStorageMapper.selectById(storageId);
    }

    // ==================== 定时清理 ====================

    @Scheduled(cron = "0 0 2 * * ?")
    @Override
    public void cleanupTempFiles() {
        Path tempRoot = Paths.get(storageRoot, ".upload-temp");
        
        if (!Files.exists(tempRoot)) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long expirationTime = 24 * 60 * 60 * 1000; // 24小时
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempRoot)) {
            for (Path tempDir : stream) {
                if (Files.isDirectory(tempDir)) {
                    try {
                        long lastModified = Files.getLastModifiedTime(tempDir).toMillis();
                        
                        if (currentTime - lastModified > expirationTime) {
                            deleteDirectory(tempDir.toFile());
                            System.out.println("清理过期临时目录: " + tempDir.getFileName());
                        }
                    } catch (IOException e) {
                        System.err.println("清理临时目录失败: " + tempDir + ", " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("扫描临时目录失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 保存物理文件到存储目录
     * 使用MD5的前两位作为子目录
     */
    private String savePhysicalFile(MultipartFile file, String fileMd5) {
        try {
            // 创建存储路径：storage/0a/0a9f3d8e...
            String subDir = fileMd5.substring(0, 2);
            Path dirPath = Paths.get(storageRoot, "storage", subDir);
            Files.createDirectories(dirPath);

            // 保存文件
            Path filePath = dirPath.resolve(fileMd5);
            file.transferTo(filePath.toFile());

            return filePath.toString();
        } catch (IOException e) {
            throw new CustomException(500, "保存文件失败: " + e.getMessage());
        }
    }

    /**
     * 合并分片文件
     */
    private String mergeChunks(UploadSession session) throws IOException {
        // 创建最终文件路径
        String subDir = session.getFileMd5().substring(0, 2);
        Path dirPath = Paths.get(storageRoot, "storage", subDir);
        Files.createDirectories(dirPath);
        
        Path finalPath = dirPath.resolve(session.getFileMd5());
        Path tempDir = Paths.get(storageRoot, ".upload-temp", session.getFileMd5());
        
        // 流式合并分片
        try (OutputStream fos = new BufferedOutputStream(
                new FileOutputStream(finalPath.toFile()), BUFFER_SIZE)) {
            
            for (int i = 0; i < session.getTotalChunks(); i++) {
                Path chunkFile = tempDir.resolve("chunk-" + i);
                if (!Files.exists(chunkFile)) {
                    throw new CustomException(400, "分片文件缺失: chunk-" + i);
                }
                
                try (InputStream fis = new BufferedInputStream(
                        new FileInputStream(chunkFile.toFile()), BUFFER_SIZE)) {
                    
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        
        return finalPath.toString();
    }

    /**
     * 递归删除目录
     */
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

    // ==================== 内部类 ====================

    /**
     * 上传会话
     */
    private static class UploadSession {
        private String uploadId;
        private String fileName;
        private Long fileSize;
        private String fileMd5;
        private int totalChunks;
        private Set<Integer> uploadedChunks;
        private long startTime;

        public String getUploadId() { return uploadId; }
        public void setUploadId(String uploadId) { this.uploadId = uploadId; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getFileMd5() { return fileMd5; }
        public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }
        
        public int getTotalChunks() { return totalChunks; }
        public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }
        
        public Set<Integer> getUploadedChunks() { return uploadedChunks; }
        public void setUploadedChunks(Set<Integer> uploadedChunks) { this.uploadedChunks = uploadedChunks; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
    }
}

