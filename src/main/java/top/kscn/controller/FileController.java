package top.kscn.controller;

import top.kscn.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.dto.FileItemDTO;
import top.kscn.entity.dto.FileTreeDTO;
import top.kscn.service.FileService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    // 列目录
    @GetMapping("/list")
    public Result<List<FileItemDTO>> list(@RequestParam(defaultValue = "/") String path) {
        List<FileItemDTO> list = fileService.listDirectory(path);
        return Result.success("获取目录成功", list);
    }

    // 新建文件夹
    @PostMapping("/folder")
    public Result<?> createFolder(@RequestParam String path, @RequestParam String name) {
        fileService.createFolder(path, name);
        return Result.success("文件夹创建成功");
    }

    // 上传文件
//    @PostMapping("/upload")
//    public Result<?> upload(@RequestParam String path, @RequestParam MultipartFile file) {
//        fileService.uploadFile(path, file);
//        return Result.success("文件上传成功");
//    }

    // 下载文件
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) {
        Resource resource = fileService.downloadFile(path);
        String filename = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(resource);
    }

    // 删除文件/文件夹
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam String path) {
        fileService.delete(path);
        return Result.success("删除成功");
    }

    // 批量上传文件
    @PostMapping("/upload/batch")
    public Result<?> uploadBatch(@RequestParam String path, @RequestParam MultipartFile[] files) {
        fileService.uploadFiles(path, files);
        return Result.success("文件上传成功");
    }

    // 上传文件夹
    @PostMapping("/upload/folder")
    public Result<?> uploadFolder(@RequestParam String path, @RequestParam MultipartFile folder) {
        fileService.uploadFolder(path, folder);
        return Result.success("文件夹上传成功");
    }

    // 批量下载
    @PostMapping("/download/batch")
    public ResponseEntity<Resource> downloadBatch(@RequestBody List<String> paths) {
        Resource resource = fileService.downloadFiles(paths);
        String filename = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(resource);
    }

    // 批量删除
    @DeleteMapping("/delete/batch")
    public Result<?> deleteBatch(@RequestBody List<String> paths) {
        fileService.deleteBatch(paths);
        return Result.success("批量删除成功");
    }

    // 下载文件夹
    @GetMapping("/download/folder")
    public ResponseEntity<Resource> downloadFolder(@RequestParam String path) {
        Resource resource = fileService.downloadFolder(path);
        String filename = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(resource);
    }

    // 重命名
    @PutMapping("/rename")
    public Result<?> rename(@RequestParam String path, @RequestParam String newName) {
        fileService.rename(path, newName);
        return Result.success("重命名成功");
    }

    // 搜索
    @GetMapping("/search")
    public Result<List<FileItemDTO>> search(@RequestParam String keyword,
                                            @RequestParam(defaultValue = "/") String basePath) {
        List<FileItemDTO> results = fileService.search(keyword, basePath);
        return Result.success("搜索完成", results);
    }

    // 获取文件树
    @GetMapping("/tree")
    public Result<List<FileTreeDTO>> getFileTree(@RequestParam(defaultValue = "/") String basePath) {
        List<FileTreeDTO> tree = fileService.getFileTree(basePath);
        return Result.success("获取文件树成功", tree);
    }
}
