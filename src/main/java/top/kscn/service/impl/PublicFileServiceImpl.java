package top.kscn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.FileStorage;
import top.kscn.entity.PublicFile;
import top.kscn.exception.CustomException;
import top.kscn.mapper.PublicFileMapper;
import top.kscn.service.FileStorageService;
import top.kscn.service.PublicFileService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 公共文件服务实现
 * 使用FileStorageService处理物理文件操作
 */
@Service
public class PublicFileServiceImpl implements PublicFileService {

    @Autowired
    private PublicFileMapper publicFileMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${file.storage-root}")
    private String storageRoot;

    // 批量下载任务缓存
    private final Map<String, BatchDownloadTask> batchDownloadTasks = new ConcurrentHashMap<>();

    // 下载缓冲区大小
    private static final int BUFFER_SIZE = 8192;

    // ==================== 文件上传 ====================

    @Override
    @Transactional
    public PublicFile uploadFile(Long parentId, MultipartFile file, String fileMd5, String category) {
        // 1. 检查文件名是否重复
        String fileName = file.getOriginalFilename();
        PublicFile existing = publicFileMapper.selectByParentIdAndName(parentId, fileName);
        if (existing != null) {
            throw new CustomException(400, "文件名已存在");
        }

        // 2. 使用FileStorageService上传物理文件（自动处理秒传和去重）
        FileStorage storage = fileStorageService.uploadFile(file, fileMd5);

        // 3. 创建公共文件记录
        String fileType = getFileExtension(fileName);
        PublicFile publicFile = new PublicFile(parentId, fileName, storage.getStorageId(), file.getSize(), fileType, category);
        publicFileMapper.insert(publicFile);

        return publicFile;
    }

    @Override
    public Map<String, Object> checkInstantUpload(String fileMd5, Long fileSize) {
        // 直接委托给FileStorageService
        return fileStorageService.checkInstantUpload(fileMd5, fileSize);
    }

    @Override
    @Transactional
    public PublicFile createFileFromStorage(Long parentId, String fileName, Long storageId, String category) {
        // 1. 获取物理文件信息
        FileStorage storage = fileStorageService.getById(storageId);
        if (storage == null || storage.getStatus() != 1) {
            throw new CustomException(404, "物理文件不存在");
        }

        // 2. 检查文件名是否重复
        PublicFile existing = publicFileMapper.selectByParentIdAndName(parentId, fileName);
        if (existing != null) {
            throw new CustomException(400, "文件名已存在");
        }

        // 3. 增加引用计数
        fileStorageService.incrementRefCount(storageId);

        // 4. 创建公共文件记录
        String fileType = getFileExtension(fileName);
        PublicFile publicFile = new PublicFile(parentId, fileName, storageId, storage.getFileSize(), fileType, category);
        publicFileMapper.insert(publicFile);

        return publicFile;
    }

    @Override
    public Map<String, Object> initChunkedUpload(String fileName, Long fileSize, String fileMd5) {
        // 直接委托给FileStorageService
        return fileStorageService.initChunkedUpload(fileName, fileSize, fileMd5);
    }

    @Override
    @Transactional
    public PublicFile completeChunkedUpload(Long parentId, String fileName, String uploadId, String category) {
        // 1. 完成分片上传，获取物理文件记录
        FileStorage storage = fileStorageService.completeChunkedUpload(uploadId);

        // 2. 检查文件名是否重复
        PublicFile existing = publicFileMapper.selectByParentIdAndName(parentId, fileName);
        if (existing != null) {
            // 文件名重复，减少引用计数
            fileStorageService.decrementRefCount(storage.getStorageId());
            throw new CustomException(400, "文件名已存在");
        }

        // 3. 创建公共文件记录
        String fileType = getFileExtension(fileName);
        PublicFile publicFile = new PublicFile(parentId, fileName, storage.getStorageId(), storage.getFileSize(), fileType, category);
        publicFileMapper.insert(publicFile);

        return publicFile;
    }

    // ==================== 文件夹操作 ====================

    @Override
    @Transactional
    public PublicFile createFolder(Long parentId, String folderName) {
        // 检查文件夹名是否重复
        PublicFile existing = publicFileMapper.selectByParentIdAndName(parentId, folderName);
        if (existing != null) {
            throw new CustomException(400, "文件夹名已存在");
        }

        // 创建文件夹
        PublicFile folder = new PublicFile(parentId, folderName);
        publicFileMapper.insert(folder);
        return folder;
    }

    @Override
    public List<PublicFile> listFiles(Long parentId) {
        List<PublicFile> files = publicFileMapper.selectByParentId(parentId);
        
        // 为文件夹设置项目数量
        for (PublicFile file : files) {
            if (file.getIsFolder() == 1) {
                int count = publicFileMapper.countByParentId(file.getFileId());
                file.setItemCount(count);
            }
        }
        
        return files;
    }

    @Override
    public List<Map<String, Object>> getDirectoryTree() {
        // 获取所有文件夹（未删除的）
        List<PublicFile> allFolders = publicFileMapper.selectAllFolders();
        
        // 构建树形结构
        return buildTree(allFolders, null);
    }
    
    /**
     * 递归构建目录树
     */
    private List<Map<String, Object>> buildTree(List<PublicFile> allFolders, Long parentId) {
        List<Map<String, Object>> tree = new java.util.ArrayList<>();
        
        for (PublicFile folder : allFolders) {
            // 找到当前层级的文件夹
            if ((parentId == null && folder.getParentId() == null) || 
                (parentId != null && parentId.equals(folder.getParentId()))) {
                
                Map<String, Object> node = new HashMap<>();
                node.put("id", folder.getFileId());
                node.put("label", folder.getFileName());
                node.put("path", folder.getFileId().toString());
                node.put("type", "folder");
                
                // 递归获取子文件夹
                List<Map<String, Object>> children = buildTree(allFolders, folder.getFileId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                
                tree.add(node);
            }
        }
        
        return tree;
    }

    @Override
    public List<PublicFile> listAllFiles() {
        return publicFileMapper.selectAll();
    }

    @Override
    public List<PublicFile> listFilesByCategory(String category) {
        return publicFileMapper.selectByCategory(category);
    }

    @Override
    public List<PublicFile> searchFiles(String keyword) {
        List<PublicFile> files = publicFileMapper.searchByKeyword(keyword);
        
        // 为文件夹设置项目数量
        for (PublicFile file : files) {
            if (file.getIsFolder() == 1) {
                int count = publicFileMapper.countByParentId(file.getFileId());
                file.setItemCount(count);
            }
        }
        
        return files;
    }

    @Override
    public PublicFile getFileById(Long fileId) {
        PublicFile file = publicFileMapper.selectById(fileId);
        if (file == null) {
            throw new CustomException(404, "文件不存在");
        }
        return file;
    }

    // ==================== 文件操作 ====================

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        PublicFile publicFile = publicFileMapper.selectById(fileId);
        if (publicFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        if (publicFile.getIsFolder() == 1) {
            // 文件夹：递归删除
            deleteFolderRecursive(fileId);
        } else {
            // 文件：软删除
            publicFileMapper.softDelete(fileId);
        }
    }

    @Override
    @Transactional
    public void permanentDeleteFile(Long fileId) {
        PublicFile publicFile = publicFileMapper.selectById(fileId);
        if (publicFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        if (publicFile.getIsFolder() == 1) {
            // 文件夹：递归彻底删除
            permanentDeleteFolderRecursive(fileId);
        } else {
            // 文件：减少引用计数，删除记录
            if (publicFile.getStorageId() != null) {
                fileStorageService.decrementRefCount(publicFile.getStorageId());
            }

            // 删除记录
            publicFileMapper.permanentDelete(fileId);
        }
    }

    @Override
    @Transactional
    public void renameFile(Long fileId, String newName) {
        PublicFile publicFile = publicFileMapper.selectById(fileId);
        if (publicFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        // 检查新名称是否重复
        PublicFile existing = publicFileMapper.selectByParentIdAndName(publicFile.getParentId(), newName);
        if (existing != null && !existing.getFileId().equals(fileId)) {
            throw new CustomException(400, "文件名已存在");
        }

        publicFileMapper.updateFileName(fileId, newName);
    }

    @Override
    @Transactional
    public void moveFile(Long fileId, Long targetParentId) {
        PublicFile publicFile = publicFileMapper.selectById(fileId);
        if (publicFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        // 检查目标文件夹是否存在
        if (targetParentId != null) {
            PublicFile targetFolder = publicFileMapper.selectById(targetParentId);
            if (targetFolder == null || targetFolder.getIsFolder() != 1) {
                throw new CustomException(404, "目标文件夹不存在");
            }
        }

        // 检查目标位置是否有同名文件
        PublicFile existing = publicFileMapper.selectByParentIdAndName(targetParentId, publicFile.getFileName());
        if (existing != null) {
            throw new CustomException(400, "目标位置已存在同名文件");
        }

        publicFileMapper.updateParentId(fileId, targetParentId);
    }

    // ==================== 文件下载 ====================

    @Override
    @Transactional
    public void downloadFile(Long fileId, String range, HttpServletResponse response) throws IOException {
        PublicFile publicFile = publicFileMapper.selectById(fileId);
        if (publicFile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        if (publicFile.getIsFolder() == 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不能下载文件夹");
            return;
        }

        // 增加下载次数
        publicFileMapper.incrementDownloadCount(fileId);

        // 使用FileStorageService下载
        fileStorageService.downloadFile(publicFile.getStorageId(), publicFile.getFileName(), range, response);
    }

    @Override
    public void downloadFolder(Long folderId, String range, HttpServletResponse response) throws IOException {
        PublicFile folder = publicFileMapper.selectById(folderId);
        if (folder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件夹不存在");
            return;
        }

        if (folder.getIsFolder() != 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不是文件夹");
            return;
        }

        // 生成任务ID（使用文件夹ID的MD5）
        String taskId = generateFolderTaskId(folderId);

        // 检查是否已存在该任务
        BatchDownloadTask existingTask = batchDownloadTasks.get(taskId);
        if (existingTask != null && "completed".equals(existingTask.getStatus())) {
            // 任务已完成，检查文件是否还存在
            Path zipPath = Paths.get(existingTask.getZipFilePath());
            if (Files.exists(zipPath)) {
                // 直接下载已有的压缩包
                downloadZipFile(zipPath.toFile(), folder.getFileName() + ".zip", range, response);
                return;
            }
        }

        // 创建新任务并立即打包
        try {
            // 创建临时目录
            Path tempDir = Paths.get(storageRoot, "temp");
            Files.createDirectories(tempDir);

            // 创建ZIP文件
            Path zipFile = tempDir.resolve(taskId + ".zip");

            // 打包文件夹
            try (ZipOutputStream zos = new ZipOutputStream(
                    new BufferedOutputStream(new FileOutputStream(zipFile.toFile()), BUFFER_SIZE))) {

                // 递归添加文件夹内容
                addFolderToZip(zos, folder, folder.getFileName());
                zos.flush();
            }

            // 缓存任务信息
            BatchDownloadTask task = new BatchDownloadTask(taskId, Collections.singletonList(folderId));
            task.setStatus("completed");
            task.setZipFilePath(zipFile.toString());
            task.setFileSize(Files.size(zipFile));
            task.setCompleteTime(System.currentTimeMillis());
            batchDownloadTasks.put(taskId, task);

            // 下载ZIP文件
            downloadZipFile(zipFile.toFile(), folder.getFileName() + ".zip", range, response);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "打包文件夹失败: " + e.getMessage());
        }
    }

    /**
     * 下载ZIP文件（支持断点续传）
     */
    private void downloadZipFile(File zipFile, String fileName, String range, HttpServletResponse response) throws IOException {
        long fileLength = zipFile.length();
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
        response.setContentType("application/zip");
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
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(zipFile, "r");
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

    /**
     * 生成文件夹任务ID
     */
    private String generateFolderTaskId(Long folderId) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(("folder_" + folderId).getBytes(StandardCharsets.UTF_8));

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "folder_" + folderId + "_" + System.currentTimeMillis();
        }
    }

    // ==================== 回收站 ====================

    @Override
    public List<PublicFile> getRecycleBin() {
        return publicFileMapper.selectDeleted();
    }

    @Override
    @Transactional
    public void restoreFile(Long fileId) {
        PublicFile publicFile = publicFileMapper.selectById(fileId);
        if (publicFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        publicFileMapper.restore(fileId);
    }

    @Override
    @Transactional
    public void emptyRecycleBin() {
        List<PublicFile> deletedFiles = publicFileMapper.selectDeleted();

        for (PublicFile file : deletedFiles) {
            permanentDeleteFile(file.getFileId());
        }
    }

    // ==================== 统计信息 ====================

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFiles", publicFileMapper.countFiles());
        stats.put("totalSize", publicFileMapper.sumFileSize());
        stats.put("totalDownloads", publicFileMapper.sumDownloadCount());
        return stats;
    }

    // ==================== 私有方法 ====================

    /**
     * 递归软删除文件夹
     */
    private void deleteFolderRecursive(Long folderId) {
        List<PublicFile> children = publicFileMapper.selectByParentId(folderId);

        for (PublicFile child : children) {
            if (child.getIsFolder() == 1) {
                deleteFolderRecursive(child.getFileId());
            } else {
                publicFileMapper.softDelete(child.getFileId());
            }
        }

        publicFileMapper.softDelete(folderId);
    }

    /**
     * 递归彻底删除文件夹
     */
    private void permanentDeleteFolderRecursive(Long folderId) {
        List<PublicFile> children = publicFileMapper.selectByParentId(folderId);

        for (PublicFile child : children) {
            if (child.getIsFolder() == 1) {
                permanentDeleteFolderRecursive(child.getFileId());
            } else {
                // 减少引用计数
                if (child.getStorageId() != null) {
                    fileStorageService.decrementRefCount(child.getStorageId());
                }

                // 删除记录
                publicFileMapper.permanentDelete(child.getFileId());
            }
        }

        publicFileMapper.permanentDelete(folderId);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    // ==================== 批量下载 ====================

    @Override
    public Map<String, Object> createBatchDownloadTask(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new CustomException(400, "文件列表不能为空");
        }

        // 生成任务ID（使用文件ID列表的MD5作为任务ID，实现复用）
        String taskId = generateTaskId(fileIds);

        // 检查是否已存在该任务
        BatchDownloadTask existingTask = batchDownloadTasks.get(taskId);
        if (existingTask != null && "completed".equals(existingTask.getStatus())) {
            // 任务已完成，检查文件是否还存在
            Path zipPath = Paths.get(existingTask.getZipFilePath());
            if (Files.exists(zipPath)) {
                Map<String, Object> result = new HashMap<>();
                result.put("taskId", taskId);
                result.put("status", "completed");
                result.put("message", "使用已有压缩包");
                return result;
            }
        }

        // 创建新任务
        BatchDownloadTask task = new BatchDownloadTask(taskId, fileIds);
        batchDownloadTasks.put(taskId, task);

        // 异步执行打包任务
        new Thread(() -> {
            try {
                task.setStatus("processing");

                // 创建临时目录
                Path tempDir = Paths.get(storageRoot, "temp");
                Files.createDirectories(tempDir);

                // 创建ZIP文件
                Path zipFile = tempDir.resolve(taskId + ".zip");

                // 打包文件
                try (ZipOutputStream zos = new ZipOutputStream(
                        new BufferedOutputStream(new FileOutputStream(zipFile.toFile()), BUFFER_SIZE))) {

                    for (Long fileId : fileIds) {
                        try {
                            PublicFile publicFile = publicFileMapper.selectById(fileId);
                            if (publicFile == null || publicFile.getIsDeleted() == 1) {
                                continue;
                            }

                            if (publicFile.getIsFolder() == 1) {
                                // 文件夹：递归添加
                                addFolderToZip(zos, publicFile, publicFile.getFileName());
                            } else {
                                // 文件：直接添加
                                addFileToZip(zos, publicFile, publicFile.getFileName());
                            }
                        } catch (Exception e) {
                            System.err.println("打包文件失败: " + fileId + ", " + e.getMessage());
                        }
                    }

                    zos.flush();
                }

                // 任务完成
                task.setStatus("completed");
                task.setZipFilePath(zipFile.toString());
                task.setFileSize(Files.size(zipFile));
                task.setCompleteTime(System.currentTimeMillis());

            } catch (Exception e) {
                task.setStatus("failed");
                task.setErrorMessage(e.getMessage());
                System.err.println("批量下载任务失败: " + taskId + ", " + e.getMessage());
            }
        }).start();

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("status", "processing");
        result.put("message", "正在打包文件...");
        return result;
    }

    @Override
    public void downloadBatchTask(String taskId, String range, HttpServletResponse response) throws IOException {
        BatchDownloadTask task = batchDownloadTasks.get(taskId);

        if (task == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "任务不存在");
            return;
        }

        if (!"completed".equals(task.getStatus())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "任务未完成，当前状态: " + task.getStatus());
            return;
        }

        File zipFile = new File(task.getZipFilePath());
        if (!zipFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        long fileLength = zipFile.length();
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
        response.setContentType("application/zip");
        response.setHeader("Accept-Ranges", "bytes");

        String fileName = URLEncoder.encode("batch_download.zip", StandardCharsets.UTF_8.toString());
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        if (range != null) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        response.setHeader("Content-Length", String.valueOf(contentLength));

        // 流式读取并写入响应
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(zipFile, "r");
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

    /**
     * 递归添加文件夹到ZIP
     */
    private void addFolderToZip(ZipOutputStream zos, PublicFile folder, String basePath) throws IOException {
        List<PublicFile> children = publicFileMapper.selectByParentId(folder.getFileId());

        for (PublicFile child : children) {
            String childPath = basePath + "/" + child.getFileName();

            if (child.getIsFolder() == 1) {
                // 子文件夹：递归添加
                addFolderToZip(zos, child, childPath);
            } else {
                // 文件：添加到ZIP
                addFileToZip(zos, child, childPath);
            }
        }
    }

    /**
     * 添加文件到ZIP
     */
    private void addFileToZip(ZipOutputStream zos, PublicFile publicFile, String entryName) throws IOException {
        // 获取物理文件
        FileStorage storage = fileStorageService.getById(publicFile.getStorageId());
        if (storage == null || storage.getStatus() != 1) {
            return;
        }

        File physicalFile = new File(storage.getFilePath());
        if (!physicalFile.exists()) {
            return;
        }

        // 创建ZIP条目
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        // 写入文件内容
        try (InputStream fis = new BufferedInputStream(
                new FileInputStream(physicalFile), BUFFER_SIZE)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }

        zos.closeEntry();
    }

    /**
     * 生成任务ID（基于文件ID列表的MD5）
     */
    private String generateTaskId(List<Long> fileIds) {
        try {
            // 排序文件ID列表以确保相同的文件列表生成相同的ID
            List<Long> sortedIds = new ArrayList<>(fileIds);
            Collections.sort(sortedIds);

            // 计算MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (Long id : sortedIds) {
                md.update(id.toString().getBytes(StandardCharsets.UTF_8));
            }

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // 如果MD5计算失败，使用UUID
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 定时清理批量下载任务
     * 每小时执行一次，删除超过1小时的已完成任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupBatchDownloadTasks() {
        long currentTime = System.currentTimeMillis();
        long expirationTime = 60 * 60 * 1000; // 1小时

        List<String> tasksToRemove = new ArrayList<>();

        for (Map.Entry<String, BatchDownloadTask> entry : batchDownloadTasks.entrySet()) {
            BatchDownloadTask task = entry.getValue();

            // 清理超过1小时的已完成或失败任务
            if (("completed".equals(task.getStatus()) || "failed".equals(task.getStatus()))
                    && currentTime - task.getCompleteTime() > expirationTime) {

                tasksToRemove.add(entry.getKey());

                // 删除ZIP文件
                if (task.getZipFilePath() != null) {
                    try {
                        Files.deleteIfExists(Paths.get(task.getZipFilePath()));
                        System.out.println("清理批量下载任务: " + entry.getKey());
                    } catch (IOException e) {
                        System.err.println("删除ZIP文件失败: " + task.getZipFilePath() + ", " + e.getMessage());
                    }
                }
            }

            // 清理超过24小时的未完成任务
            if ("pending".equals(task.getStatus()) || "processing".equals(task.getStatus())) {
                if (currentTime - task.getCreateTime() > 24 * 60 * 60 * 1000) {
                    tasksToRemove.add(entry.getKey());
                    System.out.println("清理超时任务: " + entry.getKey());
                }
            }
        }

        // 移除任务
        for (String taskId : tasksToRemove) {
            batchDownloadTasks.remove(taskId);
        }
    }

    // ==================== 内部类 ====================

    /**
     * 批量下载任务
     */
    private static class BatchDownloadTask {
        private String taskId;
        private List<Long> fileIds;
        private String status; // pending, processing, completed, failed
        private String zipFilePath;
        private long fileSize;
        private long createTime;
        private long completeTime;
        private String errorMessage;

        public BatchDownloadTask(String taskId, List<Long> fileIds) {
            this.taskId = taskId;
            this.fileIds = fileIds;
            this.status = "pending";
            this.createTime = System.currentTimeMillis();
        }

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }

        public List<Long> getFileIds() { return fileIds; }
        public void setFileIds(List<Long> fileIds) { this.fileIds = fileIds; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getZipFilePath() { return zipFilePath; }
        public void setZipFilePath(String zipFilePath) { this.zipFilePath = zipFilePath; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public long getCreateTime() { return createTime; }
        public void setCreateTime(long createTime) { this.createTime = createTime; }

        public long getCompleteTime() { return completeTime; }
        public void setCompleteTime(long completeTime) { this.completeTime = completeTime; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
