package top.kscn.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.dto.FileItemDTO;
import top.kscn.entity.dto.FileTreeDTO;
import top.kscn.exception.CustomException;
import top.kscn.service.FileService;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.root-path}")
    private String rootPath;

    // Windows系统下的非法字符
    private static final String WINDOWS_ILLEGAL_CHARS = "[/\\\\:*?\"<>|]";
    // Linux系统下的非法字符
    private static final String LINUX_ILLEGAL_CHARS = "/";
    // 通用非法字符（适用于所有系统）
    private static final String COMMON_ILLEGAL_CHARS = "[\\x00-\\x1F\\x7F]"; // 控制字符

    /**
     * 验证文件名/文件夹名是否合法
     * 根据操作系统使用不同的验证规则
     */
    private void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new CustomException(400, "名称不能为空");
        }

        // 检查通用非法字符（控制字符）
        if (name.matches(".*" + COMMON_ILLEGAL_CHARS + ".*")) {
            throw new CustomException(400, "名称包含非法字符");
        }

        // 根据操作系统检查特定非法字符
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows系统
            if (name.matches(".*" + WINDOWS_ILLEGAL_CHARS + ".*")) {
                throw new CustomException(400, "名称包含Windows系统非法字符: \\ / : * ? \" < > |");
            }

            // Windows保留名称检查
            String[] reservedNames = {"CON", "PRN", "AUX", "NUL",
                    "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                    "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
            String upperName = name.toUpperCase();
            for (String reserved : reservedNames) {
                if (upperName.equals(reserved)) {
                    throw new CustomException(400, "名称是Windows系统保留名称: " + reserved);
                }
            }
        } else {
            // Linux/Unix系统
            if (name.contains("/")) {
                throw new CustomException(400, "名称不能包含斜杠(/)");
            }
        }

        // 检查名称长度
        if (name.length() > 255) {
            throw new CustomException(400, "名称长度不能超过255个字符");
        }

        // 检查开头和结尾字符
        if (name.startsWith(".") || name.startsWith(" ") ||
                name.endsWith(".") || name.endsWith(" ")) {
            throw new CustomException(400, "名称不能以点或空格开头或结尾");
        }

        // 对连续点的检查
        if (name.contains("..")) {
            throw new CustomException(400, "名称不能包含连续的点: ..");
        }
    }

    /**
     * 验证路径格式
     * - 必须以/开头
     * - 不能包含/../
     * - 不能包含空路径段
     */
    private void validatePathFormat(String relativePath) {
        if (relativePath == null) {
            throw new CustomException(400, "路径不能为空");
        }

        // 必须以/开头
        if (!relativePath.startsWith("/")) {
            throw new CustomException(400, "路径必须以/开头: " + relativePath);
        }

        // 不能包含/../
        if (relativePath.contains("/../")) {
            throw new CustomException(400, "路径不能包含/../: " + relativePath);
        }

        // 不能包含\
//        if (relativePath.contains("\\")) {
//            throw new CustomException(400, "路径不能包含反斜杠: " + relativePath);
//        }

        // 不能有空路径段 (如 //)
        if (relativePath.contains("//")) {
            throw new CustomException(400, "路径不能包含空路径段: " + relativePath);
        }

        // 根目录特殊情况处理
        if (!"/".equals(relativePath)) {
            // 不能以/结尾 (除了根目录)
            if (relativePath.endsWith("/")) {
                throw new CustomException(400, "路径不能以/结尾: " + relativePath);
            }
        }
    }

    /**
     * 安全解析路径
     * - 支持 "/" 映射为 root
     * - 兼容 Windows/Linux
     * - 防止路径遍历攻击
     */
    private Path resolveSafePath(String relativePath) {
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
            throw new CustomException(400, "非法路径访问: " + relativePath);
        }
        return full;
    }

    @Override
    public List<FileItemDTO> listDirectory(String relativePath) {
        // 处理根目录特殊情况
        if (relativePath == null || relativePath.isEmpty()) {
            relativePath = "/";
        }

        Path dir = resolveSafePath(relativePath);
        File folder = dir.toFile();

        if (!folder.exists()) {
            throw new CustomException(400, "目录不存在: " + relativePath);
        }
        if (!folder.isDirectory()) {
            throw new CustomException(400, "路径不是目录: " + relativePath);
        }

        File[] files = folder.listFiles();
        if (files == null) return Collections.emptyList();

        List<FileItemDTO> result = new ArrayList<>();
        for (File file : files) {
            FileItemDTO dto = new FileItemDTO();
            dto.setName(file.getName());

            // 前端返回的路径始终用 /
            String itemPath = relativePath.equals("/") ?
                    "/" + file.getName() :
                    relativePath + "/" + file.getName();
            dto.setPath(itemPath);

            dto.setType(file.isDirectory() ? "folder" : "file");
            dto.setModifiedTime(file.lastModified());

            if (file.isFile()) {
                dto.setSize(file.length());
            } else {
                File[] subFiles = file.listFiles();
                dto.setItemCount(subFiles != null ? subFiles.length : 0);
            }
            result.add(dto);
        }
        return result;
    }

    @Override
    public void createFolder(String relativePath, String folderName) {
        // 验证文件夹名称
        if (folderName == null || folderName.isEmpty()) {
            throw new CustomException(400, "文件夹名称不能为空");
        }

        // 使用跨平台文件名验证
        validateName(folderName);

        Path parent = resolveSafePath(relativePath);

        if (!Files.exists(parent)) {
            throw new CustomException(400, "父目录不存在: " + relativePath);
        }
        if (!Files.isDirectory(parent)) {
            throw new CustomException(400, "目标不是目录: " + relativePath);
        }

        Path newDir = parent.resolve(folderName);
        try {
            Files.createDirectory(newDir);
        } catch (FileAlreadyExistsException e) {
            throw new CustomException(400, "文件夹已存在: " + folderName);
        } catch (IOException e) {
            throw new CustomException(400, "创建文件夹失败: " + folderName);
        }
    }

//    @Override
//    public void uploadFile(String relativePath, MultipartFile file) {
//        // 验证文件名
//        String fileName = file.getOriginalFilename();
//        if (fileName == null || fileName.isEmpty()) {
//            throw new CustomException(400, "文件名不能为空");
//        }
//
//        // 使用跨平台文件名验证
//        validateName(fileName);
//
//        Path dir = resolveSafePath(relativePath);
//
//        if (!Files.exists(dir)) {
//            throw new CustomException(400, "上传目录不存在: " + relativePath);
//        }
//        if (!Files.isDirectory(dir)) {
//            throw new CustomException(400, "上传路径不是目录: " + relativePath);
//        }
//
//        Path dest = dir.resolve(fileName);
//        try {
//            file.transferTo(dest.toFile());
//        } catch (IOException e) {
//            throw new CustomException(400, "文件上传失败: " + fileName);
//        }
//    }

    @Override
    public Resource downloadFile(String relativePath) {
        Path filePath = resolveSafePath(relativePath);
        File file = filePath.toFile();

        if (!file.exists()) {
            throw new CustomException(400, "文件不存在: " + relativePath);
        }
        if (!file.isFile()) {
            throw new CustomException(400, "路径不是文件: " + relativePath);
        }
        return new FileSystemResource(file);
    }

    @Override
    public void delete(String relativePath) {
        Path target = resolveSafePath(relativePath);
        File file = target.toFile();

        if (!file.exists()) {
            throw new CustomException(400, "文件/文件夹不存在: " + relativePath);
        }

        try {
            if (file.isDirectory()) {
                // 先删除子文件
                Files.walk(target)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } else {
                Files.delete(target);
            }
        } catch (IOException e) {
            throw new CustomException(400, "删除失败: " + relativePath);
        }
    }

    @Override
    public void uploadFiles(String relativePath, MultipartFile[] files) {
        Path dir = resolveSafePath(relativePath);

        if (!Files.exists(dir)) {
            throw new CustomException(400, "上传目录不存在: " + relativePath);
        }
        if (!Files.isDirectory(dir)) {
            throw new CustomException(400, "上传路径不是目录: " + relativePath);
        }

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                continue;
            }

            validateName(fileName);

            Path dest = dir.resolve(fileName);
            try {
                file.transferTo(dest.toFile());
            } catch (IOException e) {
                throw new CustomException(400, "文件上传失败: " + fileName);
            }
        }
    }

    @Override
    public void uploadFolder(String relativePath, MultipartFile zipFile) {
        Path dir = resolveSafePath(relativePath);

        if (!Files.exists(dir)) {
            throw new CustomException(400, "上传目录不存在: " + relativePath);
        }
        if (!Files.isDirectory(dir)) {
            throw new CustomException(400, "上传路径不是目录: " + relativePath);
        }

        // 创建临时目录存放解压文件
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("upload_folder");
        } catch (IOException e) {
            throw new CustomException(500, "创建临时目录失败");
        }

        // 保存zip文件到临时目录
        File tempZipFile = new File(tempDir.toFile(), zipFile.getOriginalFilename());
        try {
            zipFile.transferTo(tempZipFile);
        } catch (IOException e) {
            throw new CustomException(400, "保存zip文件失败");
        }

        // 获取压缩包基本名称（不含扩展名）
        String zipFilename = zipFile.getOriginalFilename();
        String baseName = zipFilename.substring(0, zipFilename.lastIndexOf('.'));

        // 使用支持中文编码的方式处理Zip文件
        try (ZipFile zip = new ZipFile(tempZipFile, Charset.forName("GBK"))) { // 使用GBK编码处理中文
            // 获取所有条目并分析第一级结构
            Enumeration<? extends ZipEntry> entries = zip.entries();

            // 收集第一级条目
            Set<String> topLevelEntries = new HashSet<>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 获取第一级条目名称
                String topLevelName;
                if (entryName.contains("/")) {
                    topLevelName = entryName.substring(0, entryName.indexOf("/"));
                } else {
                    topLevelName = entryName;
                }

                topLevelEntries.add(topLevelName);
            }

            // 确定解压目标路径
            Path extractPath = dir;
            int topLevelCount = topLevelEntries.size();

            if (topLevelCount >= 2) {
                // 多个条目，解压到以压缩包名为名的目录
                extractPath = dir.resolve(baseName);
                Files.createDirectories(extractPath);
            } else if (topLevelCount == 1) {
                String singleEntry = topLevelEntries.iterator().next();

                // 检查这是文件还是目录
                boolean isDirectory = false;
                // 需要重新获取枚举器
                Enumeration<? extends ZipEntry> checkEntries = zip.entries();
                while (checkEntries.hasMoreElements()) {
                    ZipEntry entry = checkEntries.nextElement();
                    if (entry.getName().equals(singleEntry + "/")) {
                        isDirectory = true;
                        break;
                    }
                }

                if (!isDirectory || !singleEntry.equals(baseName)) {
                    // 是文件或目录名与压缩包名不同，解压到子目录
                    extractPath = dir.resolve(baseName);
                    Files.createDirectories(extractPath);
                }
                // 否则直接解压到当前目录
            }

            // 重新获取条目枚举器进行解压
            entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // 正确处理中文文件名
                String entryName = entry.getName();
                Path filePath = extractPath.resolve(entryName).normalize();

                // 安全检查，确保解压路径在目标目录内
                if (!filePath.startsWith(extractPath)) {
                    throw new CustomException(400, "非法的压缩包条目: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    try (InputStream is = zip.getInputStream(entry);
                         OutputStream os = Files.newOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new CustomException(400, "解压文件夹失败: " + e.getMessage());
        } finally {
            // 清理临时文件
            try {
                deleteTempFiles(tempDir.toFile());
            } catch (IOException e) {
                // 记录日志但不中断流程
            }
        }
    }

    private void deleteTempFiles(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteTempFiles(f);
                }
            }
        }
        Files.delete(file.toPath());
    }

    @Override
    public Resource downloadFiles(List<String> relativePaths) {
        // 创建临时zip文件
        Path tempZip;
        try {
            tempZip = Files.createTempFile("download", ".zip");
        } catch (IOException e) {
            throw new CustomException(500, "创建临时文件失败");
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip.toFile()))) {
            for (String relativePath : relativePaths) {
                Path filePath = resolveSafePath(relativePath);
                File file = filePath.toFile();

                if (!file.exists()) {
                    continue;
                }

                // 获取相对路径（去掉开头的斜杠）
                String zipEntryPath = relativePath.startsWith("/") ?
                        relativePath.substring(1) : relativePath;

                if (file.isFile()) {
                    // 添加文件，保留路径结构
                    addFileToZip(zos, file, zipEntryPath);
                } else if (file.isDirectory()) {
                    // 添加文件夹，保留路径结构
                    addFolderToZipWithStructure(zos, file, zipEntryPath);
                }
            }
        } catch (IOException e) {
            throw new CustomException(500, "创建zip文件失败");
        }

        return new FileSystemResource(tempZip.toFile()) {
            @Override
            public String getFilename() {
                return "download.zip";
            }

            @Override
            public boolean isFile() {
                return true;
            }
        };
    }

    // 保留目录结构的文件夹添加方法
    private void addFolderToZipWithStructure(ZipOutputStream zos, File folder, String basePath) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                String entryName = basePath + "/" + file.getName();
                if (file.isFile()) {
                    addFileToZip(zos, file, entryName);
                } else if (file.isDirectory()) {
                    // 为目录创建一个条目（确保空目录也能被创建）
                    ZipEntry entry = new ZipEntry(entryName + "/");
                    zos.putNextEntry(entry);
                    zos.closeEntry();

                    // 递归添加子目录
                    addFolderToZipWithStructure(zos, file, entryName);
                }
            }
        }
    }

    private void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }
        zos.closeEntry();
    }

    @Override
    public void deleteBatch(List<String> relativePaths) {
        for (String path : relativePaths) {
            try {
                delete(path);
            } catch (CustomException e) {
                // 记录错误但继续处理其他文件
                // 可以根据需求决定是否抛出异常中断批量操作
            }
        }
    }

    @Override
    public Resource downloadFolder(String relativePath) {
        Path folderPath = resolveSafePath(relativePath);
        File folder = folderPath.toFile();

        if (!folder.exists()) {
            throw new CustomException(400, "文件夹不存在: " + relativePath);
        }
        if (!folder.isDirectory()) {
            throw new CustomException(400, "路径不是文件夹: " + relativePath);
        }

        // 创建临时zip文件
        Path tempZip;
        try {
            String zipName = folder.getName() + ".zip";
            tempZip = Files.createTempFile("folder_download_", zipName);
        } catch (IOException e) {
            throw new CustomException(500, "创建临时文件失败");
        }

        // 将文件夹内容压缩到zip文件中，保留目录结构
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip.toFile()))) {
            // 获取文件夹名称作为ZIP中的根目录
            String folderName = folder.getName();

            // 递归添加文件夹内容
            addFolderToZipWithStructure(zos, folder, folderName);
        } catch (IOException e) {
            // 删除临时文件
            try {
                Files.deleteIfExists(tempZip);
            } catch (IOException ex) {
                // 记录日志
            }
            throw new CustomException(500, "创建zip文件失败: " + e.getMessage());
        }

        // 返回资源，设置文件名为文件夹名称
        String folderName = folder.getName();
        return new FileSystemResource(tempZip.toFile()) {
            @Override
            public String getFilename() {
                return folderName + ".zip";
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(getFile()) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        // 流关闭后删除临时文件
                        try {
                            Files.deleteIfExists(tempZip);
                        } catch (IOException e) {
                            // 记录日志
                        }
                    }
                };
            }
        };
    }

    @Override
    public void rename(String relativePath, String newName) {
        validateName(newName);

        Path oldPath = resolveSafePath(relativePath);
        if (!Files.exists(oldPath)) {
            throw new CustomException(400, "文件/文件夹不存在: " + relativePath);
        }

        Path parent = oldPath.getParent();
        Path newPath = parent.resolve(newName);

        if (Files.exists(newPath)) {
            throw new CustomException(400, "目标名称已存在: " + newName);
        }

        try {
            Files.move(oldPath, newPath);
        } catch (IOException e) {
            throw new CustomException(400, "重命名失败");
        }
    }

    @Override
    public List<FileItemDTO> search(String keyword, String basePath) {
        Path searchBase = resolveSafePath(basePath);
        List<FileItemDTO> results = new ArrayList<>();

        try {
            Files.walk(searchBase)
                    .filter(path -> path.getFileName().toString().contains(keyword))
                    .forEach(path -> {
                        File file = path.toFile();
                        FileItemDTO dto = new FileItemDTO();
                        dto.setName(file.getName());

                        // 转换为相对路径
                        String relativePath = "/" + searchBase.relativize(path).toString();
                        dto.setPath(relativePath.replace("\\", "/"));

                        dto.setType(file.isDirectory() ? "folder" : "file");
                        dto.setModifiedTime(file.lastModified());

                        if (file.isFile()) {
                            dto.setSize(file.length());
                        } else {
                            File[] subFiles = file.listFiles();
                            dto.setItemCount(subFiles != null ? subFiles.length : 0);
                        }

                        results.add(dto);
                    });
        } catch (IOException e) {
            throw new CustomException(500, "搜索过程中发生错误");
        }

        return results;
    }

    @Override
    public List<FileTreeDTO> getFileTree(String basePath) {
        Path baseDir = resolveSafePath(basePath);
        return buildTree(baseDir, baseDir);
    }

    private List<FileTreeDTO> buildTree(Path root, Path current) {
        List<FileTreeDTO> tree = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(current)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    FileTreeDTO node = new FileTreeDTO();
                    node.setName(path.getFileName().toString());

                    // 转换为相对路径
                    String relativePath = "/" + root.relativize(path).toString();
                    node.setPath(relativePath.replace("\\", "/"));

                    node.setChildren(buildTree(root, path));
                    tree.add(node);
                }
            }
        } catch (IOException e) {
            // 记录日志但不中断流程
        }

        return tree;
    }
}