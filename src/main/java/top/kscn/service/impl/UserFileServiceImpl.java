package top.kscn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.FileStorage;
import top.kscn.entity.User;
import top.kscn.entity.UserFile;
import top.kscn.exception.CustomException;
import top.kscn.mapper.UserFileMapper;
import top.kscn.mapper.UserMapper;
import top.kscn.service.FileStorageService;
import top.kscn.service.UserFileService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 用户文件服务实现
 * 使用FileStorageService处理物理文件操作
 */
@Service
public class UserFileServiceImpl implements UserFileService {

    @Autowired
    private UserFileMapper userFileMapper;

    @Autowired
    private UserMapper userMapper;

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
    public UserFile uploadFile(Long userId, Long parentId, MultipartFile file, String fileMd5) {
        // 1. 检查用户空间
        if (!checkUserSpace(userId, file.getSize())) {
            throw new CustomException(400, "存储空间不足");
        }

        // 2. 检查文件名是否重复
        String fileName = file.getOriginalFilename();
        UserFile existing = userFileMapper.selectByUserAndPath(userId, parentId, fileName);
        if (existing != null) {
            throw new CustomException(400, "文件名已存在");
        }

        // 3. 使用FileStorageService上传物理文件（自动处理秒传和去重）
        FileStorage storage = fileStorageService.uploadFile(file, fileMd5);

        // 4. 创建用户文件记录
        String fileType = getFileExtension(fileName);
        UserFile userFile = new UserFile(userId, parentId, fileName, storage.getStorageId(), file.getSize(), fileType);
        userFileMapper.insert(userFile);

        // 5. 更新用户空间（重新计算实际使用量）
        updateUserSpaceUsage(userId);

        return userFile;
    }

    @Override
    public Map<String, Object> checkInstantUpload(Long userId, String fileMd5, Long fileSize) {
        // 1. 检查用户空间
        Map<String, Object> result = new HashMap<>();
        if (!checkUserSpace(userId, fileSize)) {
            result.put("canInstant", false);
            result.put("message", "存储空间不足");
            return result;
        }

        // 2. 使用FileStorageService检查物理文件是否存在
        return fileStorageService.checkInstantUpload(fileMd5, fileSize);
    }

    @Override
    @Transactional
    public UserFile createFileFromStorage(Long userId, Long parentId, String fileName, Long storageId) {
        // 1. 获取物理文件信息
        FileStorage storage = fileStorageService.getById(storageId);
        if (storage == null || storage.getStatus() != 1) {
            throw new CustomException(404, "物理文件不存在");
        }

        // 2. 检查用户空间
        if (!checkUserSpace(userId, storage.getFileSize())) {
            throw new CustomException(400, "存储空间不足");
        }

        // 3. 检查文件名是否重复
        UserFile existing = userFileMapper.selectByUserAndPath(userId, parentId, fileName);
        if (existing != null) {
            throw new CustomException(400, "文件名已存在");
        }

        // 4. 增加引用计数
        fileStorageService.incrementRefCount(storageId);

        // 5. 创建用户文件记录
        String fileType = getFileExtension(fileName);
        UserFile userFile = new UserFile(userId, parentId, fileName, storageId, storage.getFileSize(), fileType);
        userFileMapper.insert(userFile);

        // 6. 更新用户空间（重新计算实际使用量）
        updateUserSpaceUsage(userId);

        return userFile;
    }

    @Override
    public Map<String, Object> initChunkedUpload(String fileName, Long fileSize, String fileMd5) {
        // 直接委托给FileStorageService
        return fileStorageService.initChunkedUpload(fileName, fileSize, fileMd5);
    }

    @Override
    @Transactional
    public UserFile completeChunkedUpload(Long userId, Long parentId, String fileName, String uploadId) {
        // 1. 完成分片上传，获取物理文件记录
        FileStorage storage = fileStorageService.completeChunkedUpload(uploadId);

        // 2. 检查用户空间
        if (!checkUserSpace(userId, storage.getFileSize())) {
            // 空间不足，减少引用计数
            fileStorageService.decrementRefCount(storage.getStorageId());
            throw new CustomException(400, "存储空间不足");
        }

        // 3. 检查文件名是否重复
        UserFile existing = userFileMapper.selectByUserAndPath(userId, parentId, fileName);
        if (existing != null) {
            // 文件名重复，减少引用计数
            fileStorageService.decrementRefCount(storage.getStorageId());
            throw new CustomException(400, "文件名已存在");
        }

        // 4. 创建用户文件记录
        String fileType = getFileExtension(fileName);
        UserFile userFile = new UserFile(userId, parentId, fileName, storage.getStorageId(), storage.getFileSize(), fileType);
        userFileMapper.insert(userFile);

        // 5. 更新用户空间（重新计算实际使用量）
        updateUserSpaceUsage(userId);

        return userFile;
    }

    // ==================== 文件夹操作 ====================

    @Override
    @Transactional
    public UserFile createFolder(Long userId, Long parentId, String folderName) {
        // 检查文件夹名是否重复
        UserFile existing = userFileMapper.selectByUserAndPath(userId, parentId, folderName);
        if (existing != null) {
            throw new CustomException(400, "文件夹名已存在");
        }

        // 创建文件夹
        UserFile folder = new UserFile(userId, parentId, folderName);
        userFileMapper.insert(folder);
        return folder;
    }

    @Override
    public List<UserFile> listFiles(Long userId, Long parentId) {
        List<UserFile> files = userFileMapper.selectByUserAndParent(userId, parentId);
        
        // 为文件夹设置项目数量
        for (UserFile file : files) {
            if (file.getIsFolder() == 1) {
                int count = userFileMapper.selectByUserAndParent(userId, file.getFileId()).size();
                file.setItemCount(count);
            }
        }
        
        return files;
    }

    @Override
    public List<Map<String, Object>> getDirectoryTree(Long userId) {
        // 获取用户的所有文件夹
        List<UserFile> allFolders = userFileMapper.selectAllFolders(userId);
        
        // 构建树形结构
        return buildTree(allFolders, null);
    }

    @Override
    public Long getFolderIdByPath(Long userId, String path) {
        if (path == null || path.equals("/") || path.isEmpty()) {
            return null; // 根目录
        }

        String[] parts = path.split("/");
        Long currentParentId = null;

        for (String part : parts) {
            if (part.isEmpty()) continue;

            UserFile folder = userFileMapper.selectByUserAndPath(userId, currentParentId, part);
            if (folder == null || folder.getIsFolder() != 1) {
                throw new CustomException(404, "路径不存在: " + path);
            }
            currentParentId = folder.getFileId();
        }

        return currentParentId;
    }

    // ==================== 文件操作 ====================

    @Override
    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        UserFile userFile = userFileMapper.selectById(fileId);
        if (userFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        // 验证权限
        if (!userFile.getUserId().equals(userId)) {
            throw new CustomException(403, "无权限操作此文件");
        }

        if (userFile.getIsFolder() == 1) {
            // 文件夹：递归删除
            deleteFolderRecursive(userId, fileId);
        } else {
            // 文件：软删除
            userFileMapper.logicDelete(fileId);
        }
    }

    @Override
    @Transactional
    public void permanentDeleteFile(Long userId, Long fileId) {
        UserFile userFile = userFileMapper.selectById(fileId);
        if (userFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        // 验证权限
        if (!userFile.getUserId().equals(userId)) {
            throw new CustomException(403, "无权限操作此文件");
        }

        if (userFile.getIsFolder() == 1) {
            // 文件夹：递归彻底删除
            permanentDeleteFolderRecursive(userId, fileId);
        } else {
            // 文件：减少引用计数，删除记录
            if (userFile.getStorageId() != null) {
                fileStorageService.decrementRefCount(userFile.getStorageId());
            }

            // 删除记录
            userFileMapper.physicalDelete(fileId);
            
            // 更新用户空间（重新计算实际使用量）
            updateUserSpaceUsage(userId);
        }
    }

    @Override
    @Transactional
    public void renameFile(Long userId, Long fileId, String newName) {
        UserFile userFile = userFileMapper.selectById(fileId);
        if (userFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        // 验证权限
        if (!userFile.getUserId().equals(userId)) {
            throw new CustomException(403, "无权限操作此文件");
        }

        // 检查新名称是否重复
        UserFile existing = userFileMapper.selectByUserAndPath(userId, userFile.getParentId(), newName);
        if (existing != null && !existing.getFileId().equals(fileId)) {
            throw new CustomException(400, "文件名已存在");
        }

        userFileMapper.updateFileName(fileId, newName);
    }

    @Override
    @Transactional
    public void moveFile(Long userId, Long fileId, Long targetParentId) {
        UserFile userFile = userFileMapper.selectById(fileId);
        if (userFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        // 验证权限
        if (!userFile.getUserId().equals(userId)) {
            throw new CustomException(403, "无权限操作此文件");
        }

        // 检查目标文件夹是否存在
        if (targetParentId != null) {
            UserFile targetFolder = userFileMapper.selectById(targetParentId);
            if (targetFolder == null || targetFolder.getIsFolder() != 1) {
                throw new CustomException(404, "目标文件夹不存在");
            }
            if (!targetFolder.getUserId().equals(userId)) {
                throw new CustomException(403, "无权限访问目标文件夹");
            }
        }

        // 检查目标位置是否有同名文件
        UserFile existing = userFileMapper.selectByUserAndPath(userId, targetParentId, userFile.getFileName());
        if (existing != null) {
            throw new CustomException(400, "目标位置已存在同名文件");
        }

        userFileMapper.updateParentId(fileId, targetParentId);
    }

    @Override
    public List<UserFile> searchFiles(Long userId, String keyword) {
        List<UserFile> files = userFileMapper.searchByKeyword(userId, keyword);
        
        // 为文件夹设置项目数量
        for (UserFile file : files) {
            if (file.getIsFolder() == 1) {
                int count = userFileMapper.selectByUserAndParent(userId, file.getFileId()).size();
                file.setItemCount(count);
            }
        }
        
        return files;
    }

    // ==================== 文件下载 ====================

    @Override
    public void downloadFile(Long userId, Long fileId, String range, HttpServletResponse response) throws IOException {
        UserFile userFile = userFileMapper.selectById(fileId);
        if (userFile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        // 验证权限
        if (!userFile.getUserId().equals(userId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权限访问此文件");
            return;
        }

        if (userFile.getIsFolder() == 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不能下载文件夹");
            return;
        }

        // 使用FileStorageService下载
        fileStorageService.downloadFile(userFile.getStorageId(), userFile.getFileName(), range, response);
    }

    @Override
    public void downloadFolder(Long userId, Long folderId, String range, HttpServletResponse response) throws IOException {
        UserFile folder = userFileMapper.selectById(folderId);
        if (folder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件夹不存在");
            return;
        }

        // 验证权限
        if (!folder.getUserId().equals(userId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权限访问此文件夹");
            return;
        }

        if (folder.getIsFolder() != 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "不是文件夹");
            return;
        }

        // 生成任务ID（使用用户ID和文件夹ID的组合）
        String taskId = generateFolderTaskId(userId, folderId);

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
                addFolderToZip(zos, userId, folder, folder.getFileName());
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

    @Override
    public Map<String, Object> createBatchDownloadTask(Long userId, List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new CustomException(400, "文件列表不能为空");
        }

        // 验证所有文件的权限
        for (Long fileId : fileIds) {
            UserFile file = userFileMapper.selectById(fileId);
            if (file == null) {
                throw new CustomException(404, "文件不存在: " + fileId);
            }
            if (!file.getUserId().equals(userId)) {
                throw new CustomException(403, "无权限访问文件: " + fileId);
            }
        }

        // 生成任务ID（使用用户ID和文件ID列表的MD5作为任务ID，实现复用）
        String taskId = generateTaskId(userId, fileIds);

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
                            UserFile userFile = userFileMapper.selectById(fileId);
                            if (userFile == null || userFile.getIsDeleted() == 1) {
                                continue;
                            }

                            // 验证权限
                            if (!userFile.getUserId().equals(userId)) {
                                continue;
                            }

                            if (userFile.getIsFolder() == 1) {
                                // 文件夹：递归添加
                                addFolderToZip(zos, userId, userFile, userFile.getFileName());
                            } else {
                                // 文件：直接添加
                                addFileToZip(zos, userFile, userFile.getFileName());
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
    public void downloadBatchTask(Long userId, String taskId, String range, HttpServletResponse response) throws IOException {
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

        // 下载ZIP文件
        downloadZipFile(zipFile, "batch_download.zip", range, response);
    }

    /**
     * 下载ZIP文件（支持断点续传）
     */
    private void downloadZipFile(File zipFile, String fileName, String range, HttpServletResponse response) throws IOException {
        long fileLength = zipFile.length();
        long start = 0;
        long end = fileLength - 1;

        // 处理Range请求
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

        if (start > end || start < 0 || end >= fileLength) {
            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }

        long contentLength = end - start + 1;

        // 设置响应头
        response.setContentType("application/zip");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "attachment; filename=" + 
            java.net.URLEncoder.encode(fileName, "UTF-8"));

        if (range != null) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        }

        response.setHeader("Content-Length", String.valueOf(contentLength));

        // 流式读取并写入响应
        try (RandomAccessFile raf = new RandomAccessFile(zipFile, "r");
             OutputStream out = response.getOutputStream()) {

            raf.seek(start);
            byte[] buffer = new byte[8192];
            long bytesToRead = contentLength;

            while (bytesToRead > 0) {
                int readSize = (int) Math.min(buffer.length, bytesToRead);
                int bytesRead = raf.read(buffer, 0, readSize);
                if (bytesRead == -1) break;

                out.write(buffer, 0, bytesRead);
                bytesToRead -= bytesRead;
            }

            out.flush();
        }
    }

    // ==================== 空间管理 ====================

    @Override
    public Map<String, Object> getUserSpaceInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException(404, "用户不存在");
        }

        // 自动纠正空间使用量（无感知纠正）
        Long actualUsedSpace = userFileMapper.calculateUserTotalFileSize(userId);
        if (actualUsedSpace == null) {
            actualUsedSpace = 0L;
        }
        
        // 如果数据库中的已用空间与实际计算不一致，自动纠正
        if (!actualUsedSpace.equals(user.getUsedSpace())) {
            userMapper.updateUsedSpace(userId, actualUsedSpace);
            user.setUsedSpace(actualUsedSpace);
        }

        Map<String, Object> spaceInfo = new HashMap<>();
        spaceInfo.put("totalSpace", user.getTotalSpace());
        spaceInfo.put("usedSpace", actualUsedSpace);
        spaceInfo.put("availableSpace", user.getTotalSpace() - actualUsedSpace);
        spaceInfo.put("usagePercentage", (double) actualUsedSpace / user.getTotalSpace() * 100);

        return spaceInfo;
    }

    @Override
    public boolean checkUserSpace(Long userId, Long fileSize) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        // 自动纠正空间使用量（无感知纠正）
        Long actualUsedSpace = userFileMapper.calculateUserTotalFileSize(userId);
        if (actualUsedSpace == null) {
            actualUsedSpace = 0L;
        }
        
        // 如果数据库中的已用空间与实际计算不一致，自动纠正
        if (!actualUsedSpace.equals(user.getUsedSpace())) {
            userMapper.updateUsedSpace(userId, actualUsedSpace);
            user.setUsedSpace(actualUsedSpace);
        }
        
        return user.getUsedSpace() + fileSize <= user.getTotalSpace();
    }

    // ==================== 回收站 ====================

    @Override
    public List<UserFile> getRecycleBin(Long userId) {
        return userFileMapper.selectDeletedFiles(userId);
    }

    @Override
    @Transactional
    public void restoreFile(Long userId, Long fileId) {
        UserFile userFile = userFileMapper.selectById(fileId);
        if (userFile == null) {
            throw new CustomException(404, "文件不存在");
        }

        // 验证权限
        if (!userFile.getUserId().equals(userId)) {
            throw new CustomException(403, "无权限操作此文件");
        }

        userFileMapper.restore(fileId);
    }

    @Override
    @Transactional
    public void emptyRecycleBin(Long userId) {
        List<UserFile> deletedFiles = userFileMapper.selectDeletedFiles(userId);

        for (UserFile file : deletedFiles) {
            permanentDeleteFile(userId, file.getFileId());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<UserFile> folders, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        
        for (UserFile folder : folders) {
            if (Objects.equals(folder.getParentId(), parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", folder.getFileId());
                node.put("label", folder.getFileName());
                node.put("path", generatePath(folder));
                node.put("type", "folder");
                
                // 递归构建子节点
                List<Map<String, Object>> children = buildTree(folders, folder.getFileId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                
                tree.add(node);
            }
        }
        
        return tree;
    }

    /**
     * 生成节点路径
     */
    private String generatePath(UserFile folder) {
        return "folder_" + folder.getFileId();
    }

    /**
     * 收集文件夹中的所有文件ID
     */
    private void collectFolderFiles(Long userId, Long folderId, List<Long> fileIds) {
        List<UserFile> children = userFileMapper.selectByUserAndParent(userId, folderId);
        
        for (UserFile child : children) {
            if (child.getIsFolder() == 1) {
                // 递归收集子文件夹
                collectFolderFiles(userId, child.getFileId(), fileIds);
            } else {
                // 添加文件ID
                fileIds.add(child.getFileId());
            }
        }
    }

    /**
     * 收集文件夹中的所有文件（用于下载）
     */
    private void collectFolderFilesForDownload(Long userId, Long folderId, List<UserFile> files, String basePath) {
        List<UserFile> children = userFileMapper.selectByUserAndParent(userId, folderId);
        
        for (UserFile child : children) {
            if (child.getIsFolder() == 1) {
                // 递归收集子文件夹
                collectFolderFilesForDownload(userId, child.getFileId(), files, basePath + "/" + child.getFileName());
            } else {
                // 添加文件
                files.add(child);
            }
        }
    }

    /**
     * 递归软删除文件夹
     */
    private void deleteFolderRecursive(Long userId, Long folderId) {
        List<UserFile> children = userFileMapper.selectByUserAndParent(userId, folderId);

        for (UserFile child : children) {
            if (child.getIsFolder() == 1) {
                deleteFolderRecursive(userId, child.getFileId());
            } else {
                userFileMapper.logicDelete(child.getFileId());
            }
        }

        userFileMapper.logicDelete(folderId);
    }

    /**
     * 递归彻底删除文件夹
     */
    private void permanentDeleteFolderRecursive(Long userId, Long folderId) {
        List<UserFile> children = userFileMapper.selectByUserAndParent(userId, folderId);

        for (UserFile child : children) {
            if (child.getIsFolder() == 1) {
                permanentDeleteFolderRecursive(userId, child.getFileId());
            } else {
                // 减少引用计数
                if (child.getStorageId() != null) {
                    fileStorageService.decrementRefCount(child.getStorageId());
                }

                // 删除记录
                userFileMapper.physicalDelete(child.getFileId());
            }
        }

        userFileMapper.physicalDelete(folderId);
        
        // 更新用户空间（重新计算实际使用量）
        updateUserSpaceUsage(userId);
    }
    
    /**
     * 更新用户空间使用量（重新计算）
     * 通过统计用户所有文件（包括回收站）的大小总和来更新已用空间
     * 注意：统计的是数据库记录的大小总和，不是物理文件大小
     */
    private void updateUserSpaceUsage(Long userId) {
        // 计算用户所有文件（包括回收站）的总大小
        Long totalUsedSpace = userFileMapper.calculateUserTotalFileSize(userId);
        if (totalUsedSpace == null) {
            totalUsedSpace = 0L;
        }
        
        // 更新用户已用空间
        userMapper.updateUsedSpace(userId, totalUsedSpace);
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

    /**
     * 递归添加文件夹到ZIP
     */
    private void addFolderToZip(ZipOutputStream zos, Long userId, UserFile folder, String basePath) throws IOException {
        List<UserFile> children = userFileMapper.selectByUserAndParent(userId, folder.getFileId());

        for (UserFile child : children) {
            String childPath = basePath + "/" + child.getFileName();

            if (child.getIsFolder() == 1) {
                // 子文件夹：递归添加
                addFolderToZip(zos, userId, child, childPath);
            } else {
                // 文件：添加到ZIP
                addFileToZip(zos, child, childPath);
            }
        }
    }

    /**
     * 添加文件到ZIP
     */
    private void addFileToZip(ZipOutputStream zos, UserFile userFile, String entryName) throws IOException {
        // 获取物理文件
        FileStorage storage = fileStorageService.getById(userFile.getStorageId());
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
     * 生成文件夹任务ID
     */
    private String generateFolderTaskId(Long userId, Long folderId) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(("user_" + userId + "_folder_" + folderId).getBytes(StandardCharsets.UTF_8));

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "user_" + userId + "_folder_" + folderId + "_" + System.currentTimeMillis();
        }
    }

    /**
     * 生成任务ID（基于用户ID和文件ID列表的MD5）
     */
    private String generateTaskId(Long userId, List<Long> fileIds) {
        try {
            // 排序文件ID列表以确保相同的文件列表生成相同的ID
            List<Long> sortedIds = new ArrayList<>(fileIds);
            Collections.sort(sortedIds);

            // 计算MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(("user_" + userId).getBytes(StandardCharsets.UTF_8));
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

