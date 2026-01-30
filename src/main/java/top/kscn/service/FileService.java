package top.kscn.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.dto.FileItemDTO;
import top.kscn.entity.dto.FileTreeDTO;

import java.util.List;
import java.util.zip.ZipOutputStream;

public interface FileService {
    List<FileItemDTO> listDirectory(String relativePath);

    void createFolder(String relativePath, String folderName);

//    void uploadFile(String relativePath, MultipartFile file);

    // 新增：批量上传文件
    void uploadFiles(String relativePath, MultipartFile[] files);

    // 新增：上传文件夹（需要前端以zip格式上传）
    void uploadFolder(String relativePath, MultipartFile zipFile);

    Resource downloadFile(String relativePath);

    // 新增：批量下载（返回zip资源）
    Resource downloadFiles(List<String> relativePaths);

    void delete(String relativePath);

    // 新增：批量删除
    void deleteBatch(List<String> relativePaths);

    // 新增：下载文件夹（返回zip资源）
    Resource downloadFolder(String relativePath);

    // 新增：重命名文件/文件夹
    void rename(String relativePath, String newName);

    // 新增：搜索文件/文件夹
    List<FileItemDTO> search(String keyword, String basePath);

    // 新增：获取文件树
    List<FileTreeDTO> getFileTree(String basePath);
}