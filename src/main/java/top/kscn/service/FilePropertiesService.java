package top.kscn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.kscn.entity.FileStorage;
import top.kscn.entity.PublicFile;
import top.kscn.entity.UserFile;
import top.kscn.exception.CustomException;
import top.kscn.mapper.FileStorageMapper;
import top.kscn.mapper.PublicFileMapper;
import top.kscn.mapper.UserFileMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class FilePropertiesService {
    private final PublicFileMapper publicFiles;
    private final UserFileMapper userFiles;
    private final FileStorageMapper storageMapper;
    @org.springframework.beans.factory.annotation.Autowired
    private RemoteFileAccess remoteFiles;
    private final Semaphore hashSlots = new Semaphore(2);
    private static final Set<String> ALGORITHMS = Set.of("MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512");

    public record Properties(Long fileId, String fileName, String fileType, long fileSize, String parentPath,
                             String fullPath, LocalDateTime createTime, LocalDateTime updateTime,
                             String category, Integer downloadCount, boolean folder, long fileCount, long folderCount,
                             boolean remote, boolean sizeKnown) {
        public Properties(Long fileId, String fileName, String fileType, long fileSize, String parentPath,
                          String fullPath, LocalDateTime createTime, LocalDateTime updateTime, String category,
                          Integer downloadCount, boolean folder, long fileCount, long folderCount) {
            this(fileId, fileName, fileType, fileSize, parentPath, fullPath, createTime, updateTime, category,
                    downloadCount, folder, fileCount, folderCount, false, true);
        }
    }
    public record HashResult(Long fileId, String algorithm, String value, long bytes, LocalDateTime calculatedAt) {}
    public static final int PREVIEW_LIMIT = 10 * 1024 * 1024;
    public record PreviewSource(String name, byte[] bytes) {}
    private record FileInfo(Long id, Long parent, String name, boolean folder, Long storageId, long size,
                            String type, LocalDateTime created, LocalDateTime updated, String category, Integer downloads,
                            boolean remote, Long remoteSize, Boolean sizeKnown) {}

    private FileInfo load(Long userId, Long fileId) {
        if (userId == null) {
            PublicFile file = publicFiles.selectById(fileId);
            if (file == null || !Objects.equals(file.getIsDeleted(), 0)) throw new CustomException(404, "文件不存在或已删除");
            return new FileInfo(file.getFileId(), file.getParentId(), file.getFileName(), Objects.equals(file.getIsFolder(), 1),
                    file.getStorageId(), file.getFileSize() == null ? 0 : file.getFileSize(), file.getFileType(),
                    file.getCreateTime(), file.getUpdateTime(), file.getCategory(), file.getDownloadCount(), file.isRemote(), file.getRemoteSize(), file.getSizeKnown());
        }
        UserFile file = userFiles.selectById(fileId);
        if (file == null || !Objects.equals(file.getUserId(), userId) || !Objects.equals(file.getIsDeleted(), 0))
            throw new CustomException(404, "文件不存在或无权访问");
        return new FileInfo(file.getFileId(), file.getParentId(), file.getFileName(), Objects.equals(file.getIsFolder(), 1),
                file.getStorageId(), file.getFileSize() == null ? 0 : file.getFileSize(), file.getFileType(),
                file.getCreateTime(), file.getUpdateTime(), null, null, file.isRemote(), file.getRemoteSize(), file.getSizeKnown());
    }

    private FileInfo file(Long userId, Long fileId) {
        FileInfo file = load(userId, fileId);
        if (file.folder()) throw new CustomException(400, "请选择单个文件，暂不支持文件夹校验");
        return file;
    }

    private String parentPath(Long userId, FileInfo file) {
        LinkedList<String> names = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        Long parent = file.parent();
        while (parent != null) {
            if (!visited.add(parent)) throw new CustomException(409, "目录结构异常，请刷新页面");
            FileInfo directory = load(userId, parent);
            if (!directory.folder()) throw new CustomException(404, "所在目录已不可用");
            names.addFirst(directory.name());
            parent = directory.parent();
        }
        names.addFirst("根目录");
        return String.join("/", names);
    }

    @Transactional(readOnly = true)
    public Properties properties(Long userId, Long fileId) {
        FileInfo file = load(userId, fileId);
        String parent = parentPath(userId, file);
        long[] totals = file.folder() ? folderTotals(userId, file.id(), new HashSet<>()) : new long[]{file.size(), 1, 0};
        boolean remote = file.remote();
        if (remote) totals[0] = file.remoteSize() == null ? 0 : file.remoteSize();
        return new Properties(file.id(), file.name(), file.type(), totals[0], parent, parent + "/" + file.name(),
                file.created(), file.updated(), file.category(), file.downloads(), file.folder(), totals[1], totals[2], remote, !remote || Boolean.TRUE.equals(file.sizeKnown()));
    }

    private long[] folderTotals(Long userId, Long folderId, Set<Long> visited) {
        if (!visited.add(folderId)) throw new CustomException(409, "目录结构异常，请刷新页面");
        long[] totals = new long[3];
        if (userId == null) {
            for (PublicFile child : publicFiles.selectByParentId(folderId))
                addChildTotals(totals, userId, child.getFileId(), Objects.equals(child.getIsFolder(), 1), child.getFileSize(), visited);
        } else {
            for (UserFile child : userFiles.selectByUserAndParent(userId, folderId))
                addChildTotals(totals, userId, child.getFileId(), Objects.equals(child.getIsFolder(), 1), child.getFileSize(), visited);
        }
        return totals;
    }

    private void addChildTotals(long[] totals, Long userId, Long id, boolean folder, Long size, Set<Long> visited) {
        if (folder) {
            long[] child = folderTotals(userId, id, visited);
            totals[0] += child[0]; totals[1] += child[1]; totals[2] += child[2] + 1;
        } else {
            totals[0] += size == null ? 0 : size; totals[1]++;
        }
    }

    @Transactional
    public PreviewSource previewSource(Long userId, Long fileId) {
        FileInfo file = file(userId, fileId);
        parentPath(userId, file);
        if (file.size() >= PREVIEW_LIMIT) throw new CustomException(413, "文件过大，仅支持小于10MB的文件，请下载后预览");
        if (file.storageId() == null) throw new CustomException(404, "文件内容不存在");
        FileStorage source = storageMapper.lockById(file.storageId());
        if (source == null || !Objects.equals(source.getStatus(), 1)) throw new CustomException(404, "文件内容不存在");
        if (source.isRemote()) return new PreviewSource(file.name(), remoteFiles.preview(source));
        try {
            Path path = Path.of(source.getFilePath());
            BasicFileAttributes before = Files.readAttributes(path, BasicFileAttributes.class);
            if (!before.isRegularFile()) throw new CustomException(404, "文件内容不存在");
            if (before.size() >= PREVIEW_LIMIT) throw new CustomException(413, "文件过大，仅支持小于10MB的文件，请下载后预览");
            byte[] bytes;
            try (InputStream input = Files.newInputStream(path)) { bytes = input.readNBytes(PREVIEW_LIMIT); }
            if (bytes.length >= PREVIEW_LIMIT) throw new CustomException(413, "文件过大，请下载后预览");
            BasicFileAttributes after = Files.readAttributes(path, BasicFileAttributes.class);
            if (bytes.length != before.size() || after.size() != before.size() || !after.lastModifiedTime().equals(before.lastModifiedTime())
                    || !Objects.equals(after.fileKey(), before.fileKey())) throw new CustomException(409, "文件发生变化，请重新预览");
            return new PreviewSource(file.name(), bytes);
        } catch (IOException exception) { throw new CustomException(500, "无法读取文件内容，请下载后重试"); }
    }

    @Transactional
    public HashResult hash(Long userId, Long fileId, String requestedAlgorithm) {
        String algorithm = requestedAlgorithm == null ? "" : requestedAlgorithm.toUpperCase(Locale.ROOT);
        if (!ALGORITHMS.contains(algorithm)) throw new CustomException(400, "不支持的校验算法");
        FileInfo file = file(userId, fileId);
        parentPath(userId, file); // 已删除/不可访问的祖先目录也不能绕过。
        if (file.storageId() == null) throw new CustomException(404, "文件内容不存在");
        if (!hashSlots.tryAcquire()) throw new CustomException(429, "当前校验任务较多，请稍后重试");
        try {
            // 锁住该源文件记录，计算期间阻止其被回收站物理清理，不锁整个文件空间。
            FileStorage source = storageMapper.lockById(file.storageId());
            if (source == null || !Objects.equals(source.getStatus(), 1)) throw new CustomException(404, "文件内容不存在");
            if (source.isRemote()) throw new CustomException(400, "远程链接内容可能变化，暂不支持哈希校验；请先网络导入到服务器");
            Path path = Path.of(source.getFilePath());
            BasicFileAttributes before = Files.readAttributes(path, BasicFileAttributes.class);
            if (!before.isRegularFile()) throw new CustomException(404, "文件内容不存在");
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            long bytes = 0;
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[64 * 1024];
                for (int count; (count = input.read(buffer)) != -1;) {
                    digest.update(buffer, 0, count);
                    bytes += count;
                }
            }
            BasicFileAttributes after = Files.readAttributes(path, BasicFileAttributes.class);
            if (bytes != before.size() || after.size() != before.size()
                    || !after.lastModifiedTime().equals(before.lastModifiedTime()) || !Objects.equals(after.fileKey(), before.fileKey()))
                throw new CustomException(409, "校验期间文件发生变化，请重新计算");
            return new HashResult(fileId, algorithm, HexFormat.of().formatHex(digest.digest()), bytes, LocalDateTime.now());
        } catch (IOException exception) {
            throw new CustomException(500, "无法读取文件内容，请稍后重试");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("校验算法不可用", exception);
        } finally { hashSlots.release(); }
    }
}
