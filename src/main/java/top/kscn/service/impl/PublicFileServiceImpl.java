package top.kscn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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
import java.util.*;

/**
 * 公共文件服务实现
 * 使用FileStorageService处理物理文件操作
 */
@Service
public class PublicFileServiceImpl implements PublicFileService {
    private static final Logger log = LoggerFactory.getLogger(PublicFileServiceImpl.class);

    @Autowired
    private top.kscn.service.RecycleBinService recycleBinService;

    @Autowired
    private PublicFileMapper publicFileMapper;

    @Autowired
    private FileStorageService fileStorageService;

    // 共享的下载缓存与生命周期管理
    @Autowired
    private top.kscn.service.DownloadArchiveService downloadArchives;


    // ==================== 文件上传 ====================

    @Override
    @Transactional
    public PublicFile uploadFile(Long parentId, MultipartFile file, String fileMd5, String category) {
        recycleBinService.lockForWrite(null, parentId);
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
        recycleBinService.lockForWrite(null, parentId);
        // 1. 获取物理文件信息
        FileStorage storage = fileStorageService.getById(storageId);
        if (storage == null || storage.getStatus() != 1 || storage.isRemote()) {
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
        recycleBinService.lockForWrite(null, parentId);
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
        recycleBinService.lockForWrite(null, parentId);
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
        recycleBinService.delete(null, fileId);
    }

    @Override
    @Transactional
    public void permanentDeleteFile(Long fileId) {
        recycleBinService.purge(null, fileId);
    }

    @Override
    @Transactional
    public void renameFile(Long fileId, String newName) {
        recycleBinService.lockForWrite(null, null);
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
        recycleBinService.lockForWrite(null, targetParentId);
        recycleBinService.checkMove(null, fileId, targetParentId);
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

        // 网络传输不放在事务中，避免下载期间一直占用文件记录的行锁和数据库连接。
        fileStorageService.downloadFile(publicFile.getStorageId(), publicFile.getFileName(), range, response);

        // 文件写出完成后再独立更新统计；统计失败不能使下载失败，也不向响应追加错误内容。
        if (response.getStatus() == HttpServletResponse.SC_OK
                || response.getStatus() == HttpServletResponse.SC_PARTIAL_CONTENT) {
            try {
                publicFileMapper.incrementDownloadCount(fileId);
            } catch (DataAccessException e) {
                log.warn("[下载] 文件已写出，下载次数更新失败，fileId={}, cause={}",
                        fileId, e.getMostSpecificCause().getMessage());
            }
        }
    }

    @Override
    public void downloadFolder(Long folderId, String range, HttpServletResponse response) throws IOException {
        downloadArchives.folder(null, folderId, range, response);
    }

    // ==================== 回收站 ====================

    @Override
    public List<PublicFile> getRecycleBin() {
        return publicFileMapper.selectDeleted();
    }

    @Override
    @Transactional
    public void restoreFile(Long fileId) {
        recycleBinService.restore(null, fileId);
    }

    @Override
    @Transactional
    public void emptyRecycleBin() {
        recycleBinService.empty(null);
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

    @Override
    public long countPublicResourceFiles() {
        return publicFileMapper.countFiles();
    }

    // ==================== 私有方法 ====================

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
        try { return downloadArchives.create(null, fileIds); }
        catch (IOException e) { throw new CustomException(500, "打包失败，请稍后重试"); }
    }

    @Override
    public void downloadBatchTask(String taskId, String range, HttpServletResponse response) throws IOException {
        downloadArchives.stream(null, taskId, "batch_download.zip", range, response);
    }

}
