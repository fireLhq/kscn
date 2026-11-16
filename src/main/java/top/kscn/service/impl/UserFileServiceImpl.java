package top.kscn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.*;

/**
 * 用户文件服务实现
 * 使用FileStorageService处理物理文件操作
 */
@Service
public class UserFileServiceImpl implements UserFileService {
    @Autowired
    private top.kscn.service.RecycleBinService recycleBinService;

    @Autowired
    private UserFileMapper userFileMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileStorageService fileStorageService;

    // 共享的下载缓存与生命周期管理
    @Autowired
    private top.kscn.service.DownloadArchiveService downloadArchives;


    // ==================== 文件上传 ====================

    @Override
    @Transactional
    public UserFile uploadFile(Long userId, Long parentId, MultipartFile file, String fileMd5) {
        recycleBinService.lockForWrite(userId, parentId);
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
        recycleBinService.lockForWrite(userId, parentId);
        // 1. 获取物理文件信息
        FileStorage storage = fileStorageService.getById(storageId);
        if (storage == null || storage.getStatus() != 1 || storage.isRemote()) {
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
        recycleBinService.lockForWrite(userId, parentId);
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
        recycleBinService.lockForWrite(userId, parentId);
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
        recycleBinService.delete(userId, fileId);
    }

    @Override
    @Transactional
    public void permanentDeleteFile(Long userId, Long fileId) {
        recycleBinService.purge(userId, fileId);
    }

    @Override
    @Transactional
    public void renameFile(Long userId, Long fileId, String newName) {
        recycleBinService.lockForWrite(userId, null);
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
        recycleBinService.lockForWrite(userId, targetParentId);
        recycleBinService.checkMove(userId, fileId, targetParentId);
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
        downloadArchives.folder(userId, folderId, range, response);
    }

    @Override
    public Map<String, Object> createBatchDownloadTask(Long userId, List<Long> fileIds) {
        try { return downloadArchives.create(userId, fileIds); }
        catch (IOException e) { throw new CustomException(500, "打包失败，请稍后重试"); }
    }

    @Override
    public void downloadBatchTask(Long userId, String taskId, String range, HttpServletResponse response) throws IOException {
        downloadArchives.stream(userId, taskId, "batch_download.zip", range, response);
    }

    // ==================== 空间管理 ====================

    @Override
    public Map<String, Object> getUserSpaceInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException(404, "用户不存在");
        }

        Long usedSpace = user.getUsedSpace() == null ? 0L : user.getUsedSpace();
        Long totalSpace = user.getTotalSpace() == null ? 0L : user.getTotalSpace();

        Map<String, Object> spaceInfo = new HashMap<>();
        spaceInfo.put("totalSpace", totalSpace);
        spaceInfo.put("usedSpace", usedSpace);
        spaceInfo.put("availableSpace", Math.max(totalSpace - usedSpace, 0L));
        spaceInfo.put("usagePercentage", totalSpace <= 0 ? 0D : (double) usedSpace / totalSpace * 100);

        return spaceInfo;
    }

    @Override
    public Long getUserFileCount(Long userId) {
        Long fileCount = userFileMapper.countUserFiles(userId);
        return fileCount == null ? 0L : fileCount;
    }

    @Override
    public Long recalculateUsedSpace(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException(404, "用户不存在");
        }
        return recalculateAndPersistUserSpaceUsage(userId);
    }

    @Override
    public boolean checkUserSpace(Long userId, Long fileSize) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        Long actualUsedSpace = recalculateAndPersistUserSpaceUsage(userId);
        Long totalSpace = user.getTotalSpace() == null ? 0L : user.getTotalSpace();
        return actualUsedSpace + fileSize <= totalSpace;
    }

    // ==================== 回收站 ====================

    @Override
    public List<UserFile> getRecycleBin(Long userId) {
        return userFileMapper.selectDeletedFiles(userId);
    }

    @Override
    @Transactional
    public void restoreFile(Long userId, Long fileId) {
        recycleBinService.restore(userId, fileId);
        updateUserSpaceUsage(userId);
    }

    @Override
    @Transactional
    public void emptyRecycleBin(Long userId) {
        recycleBinService.empty(userId);
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
     * 更新用户空间使用量（重新计算后直接覆盖）
     * 仅在用户执行文件相关操作时调用，正常读取不修改数据库值
     */
    private void updateUserSpaceUsage(Long userId) {
        recalculateAndPersistUserSpaceUsage(userId);
    }

    private Long recalculateAndPersistUserSpaceUsage(Long userId) {
        Long totalUsedSpace = userFileMapper.calculateUserTotalFileSize(userId);
        if (totalUsedSpace == null) {
            totalUsedSpace = 0L;
        }

        userMapper.setUsedSpace(userId, totalUsedSpace);
        return totalUsedSpace;
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

}
