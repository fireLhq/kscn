package top.kscn.service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.kscn.config.DownloadArchiveProperties;
import top.kscn.entity.*;
import top.kscn.exception.CustomException;
import top.kscn.exception.DownloadTransferException;
import top.kscn.mapper.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.zip.*;

/** Immutable ZIP versions, disk-backed expiry and streaming leases for both file spaces. */
@Slf4j
@Service
public class DownloadArchiveService {
    private static final Pattern TOKEN = Pattern.compile("[up]-[0-9]+-[a-f0-9]{32}-[a-f0-9]{32}-[0-9]+-[a-f0-9]{32}");
    private static final Pattern LEGACY = Pattern.compile("(?:[a-fA-F0-9]{32}|(?:user_[0-9]+_)?folder_[0-9]+_[0-9]+)\\.zip");
    private final DownloadArchiveProperties config;
    private final UserFileMapper users;
    private final PublicFileMapper publics;
    private final FileStorageMapper storage;
    private final Path directory;
    private final Object monitor = new Object();
    private final Map<Path, Integer> active = new HashMap<>();
    private final ReentrantLock[] stripes = new ReentrantLock[64];
    private final Semaphore builders;
    private long reserved;

    public DownloadArchiveService(DownloadArchiveProperties config, UserFileMapper users, PublicFileMapper publics,
                                  FileStorageMapper storage, @Value("${file.storage-root}") String root) {
        this.config = config; this.users = users; this.publics = publics; this.storage = storage;
        this.directory = Paths.get(root, "temp", "downloads").toAbsolutePath().normalize();
        this.builders = new Semaphore(config.getMaxConcurrentBuilds());
        Arrays.setAll(stripes, i -> new ReentrantLock());
    }

    @PostConstruct
    public void initialize() throws IOException { Files.createDirectories(directory); cleanup(); }

    private record Item(Long id, Long owner, String name, boolean folder, Long storageId) {}
    private record Entry(String name, Path source, long size, long modified, Long id, Long storageId) {}
    private record Pending(Item item, String parent, int depth) {}
    private record Plan(String key, String fingerprint, List<Entry> entries) {}

    private Item item(Long owner, Long id) {
        if (owner == null) {
            PublicFile f = publics.selectById(id);
            if (f == null) throw new CustomException(404, "文件不存在或已删除");
            return new Item(f.getFileId(), null, f.getFileName(), f.getIsFolder() == 1, f.getStorageId());
        }
        UserFile f = users.selectById(id);
        if (f == null) throw new CustomException(404, "文件不存在或已删除");
        if (!owner.equals(f.getUserId())) throw new CustomException(403, "无权下载其他用户的文件");
        return new Item(f.getFileId(), f.getUserId(), f.getFileName(), f.getIsFolder() == 1, f.getStorageId());
    }

    private Plan plan(Long owner, List<Long> ids) throws IOException {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(Objects::isNull)) throw new CustomException(400, "请选择要下载的文件");
        if (ids.size() > config.getMaxEntries()) throw new CustomException(413, "所选项目超过打包数量上限");
        List<Long> roots = ids.stream().distinct().sorted().toList();
        String key = (owner == null ? "p-0-" : "u-" + owner + "-") + digest(roots.toString());
        Deque<Pending> queue = new ArrayDeque<>();
        for (Long id : roots) queue.add(new Pending(item(owner, id), "", 0));
        Set<Long> visited = new HashSet<>(); Set<String> names = new HashSet<>();
        List<Entry> entries = new ArrayList<>(); long total = 0;
        while (!queue.isEmpty()) {
            Pending next = queue.removeFirst(); Item f = next.item;
            if (!visited.add(f.id)) continue; // Selecting a parent and its child must not duplicate bytes.
            if (visited.size() > config.getMaxEntries() || next.depth > 128) throw new CustomException(413, "目录层级或项目数量超过打包限制");
            String name = safeName(f.name);
            String path = next.parent + name + (f.folder ? "/" : "");
            if (!names.add(path)) { path = next.parent + f.id + "-" + name + (f.folder ? "/" : ""); if (!names.add(path)) throw new CustomException(409, "压缩包内存在重名路径"); }
            if (f.folder) {
                entries.add(new Entry(path, null, 0, 0, f.id, null));
                List<Long> children = owner == null ? publics.selectByParentId(f.id).stream().map(PublicFile::getFileId).toList()
                        : users.selectByUserAndParent(owner, f.id).stream().map(UserFile::getFileId).toList();
                if (queue.size() + children.size() + visited.size() > config.getMaxEntries() * 2L) throw new CustomException(413, "目录项目过多，请分批下载");
                for (Long child : children) queue.add(new Pending(item(owner, child), path, next.depth + 1));
            } else {
                FileStorage physical = storage.selectById(f.storageId);
                if (physical == null) throw new CustomException(404, "源文件不可用");
                if (physical.isRemote()) throw new CustomException(400, "所选文件或文件夹含远程链接，暂不支持打包；请单独下载或先网络导入到服务器");
                Path source = Paths.get(physical.getFilePath());
                if (!Files.isRegularFile(source)) throw new CustomException(404, "源文件不存在");
                long size = Files.size(source);
                if (size > config.getMaxSourceSize().toBytes() - total) throw new CustomException(413, "待打包原文件总大小超过 " + config.getMaxSourceSize() + "，请分批下载");
                total += size;
                entries.add(new Entry(path, source, size, Files.getLastModifiedTime(source).toMillis(), f.id, f.storageId));
            }
        }
        entries.sort(Comparator.comparing(Entry::name));
        return new Plan(key, digest(entries.toString()), entries);
    }

    private static String safeName(String name) {
        if (name == null || name.isBlank() || name.equals(".") || name.equals("..")
                || name.chars().anyMatch(ch -> ch < 32 || ch == 127 || ch == '/' || ch == '\\' || ch == ':'))
            throw new CustomException(400, "文件名称无法安全打包");
        return name;
    }

    private static String digest(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    /** Build before returning the token; callers never announce a still-incomplete ZIP as downloadable. */
    public Map<String, Object> create(Long owner, List<Long> ids) throws IOException {
        Plan plan = plan(owner, ids);
        ReentrantLock stripe = stripes[(plan.key.hashCode() & Integer.MAX_VALUE) % stripes.length];
        if (!stripe.tryLock()) throw new CustomException(429, "同组文件正在打包，请稍后重试");
        try {
            synchronized (monitor) {
                for (Path path : files()) {
                    if (path.getFileName().toString().startsWith(plan.key + "-" + plan.fingerprint + "-") && !expired(path)
                            && Files.size(path) <= config.getMaxArchiveSize().toBytes()) {
                        touch(path); return result(path);
                    }
                }
            }
            if (!builders.tryAcquire()) throw new CustomException(429, "打包任务繁忙，请稍后重试");
            boolean reservation = false;
            Path partial = null;
            try {
                cleanup();
                synchronized (monitor) {
                    long used = 0;
                    for (Path path : allCacheFiles()) used = Math.addExact(used, Files.size(path));
                    long max = config.getMaxArchiveSize().toBytes();
                    if (used > config.getMaxCacheSize().toBytes() - reserved - max) throw new CustomException(507, "临时下载空间不足，请稍后重试");
                    reserved += max; reservation = true;
                    long previous = files().stream().filter(p -> p.getFileName().toString().startsWith(plan.key + "-")).mapToLong(this::created).max().orElse(0);
                    String token = plan.key + "-" + plan.fingerprint + "-" + Math.max(System.currentTimeMillis(), previous + 1) + "-" + UUID.randomUUID().toString().replace("-", "");
                    partial = directory.resolve(token + ".part"); active.put(partial, 1);
                }
                writeZip(plan.entries, partial);
                Path completed = partial.resolveSibling(partial.getFileName().toString().replace(".part", ".zip"));
                synchronized (monitor) {
                    Files.move(partial, completed, StandardCopyOption.ATOMIC_MOVE);
                    touch(completed); active.remove(partial);
                    removeOlder(plan.key, completed);
                }
                return result(completed);
            } finally {
                synchronized (monitor) {
                    if (partial != null) { active.remove(partial); try { Files.deleteIfExists(partial); } catch (IOException e) { log.warn("清理未完成压缩包失败: {}", partial, e); } }
                    if (reservation) reserved -= config.getMaxArchiveSize().toBytes();
                }
                builders.release();
            }
        } finally { stripe.unlock(); }
    }

    private Map<String, Object> result(Path path) {
        String name = path.getFileName().toString();
        return Map.of("taskId", name.substring(0, name.length() - 4), "status", "completed", "message", "压缩包已就绪");
    }

    private void writeZip(List<Entry> entries, Path path) throws IOException {
        try (OutputStream file = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW);
             ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new LimitedOutput(file, config.getMaxArchiveSize().toBytes())))) {
            byte[] buffer = new byte[65536]; long total = 0;
            for (Entry entry : entries) {
                zip.putNextEntry(new ZipEntry(entry.name));
                if (entry.source != null) {
                    long copied = 0;
                    try (InputStream in = Files.newInputStream(entry.source)) {
                        int count;
                        while ((count = in.read(buffer)) != -1) {
                            if (count > config.getMaxSourceSize().toBytes() - total) throw new CustomException(413, "实际源文件大小超过打包上限");
                            total += count; copied += count; zip.write(buffer, 0, count);
                        }
                    }
                    if (copied != entry.size || Files.getLastModifiedTime(entry.source).toMillis() != entry.modified)
                        throw new CustomException(409, "文件在打包期间发生变化，请重试");
                }
                zip.closeEntry();
            }
        }
    }

    private static class LimitedOutput extends FilterOutputStream {
        private final long limit; private long count;
        LimitedOutput(OutputStream out, long limit) { super(out); this.limit = limit; }
        @Override public void write(int value) throws IOException { ensure(1); out.write(value); }
        @Override public void write(byte[] bytes, int offset, int length) throws IOException { ensure(length); out.write(bytes, offset, length); }
        private void ensure(int length) { if (length > limit - count) throw new CustomException(413, "压缩包大小超过配置上限"); count += length; }
    }

    public void folder(Long owner, Long id, String range, HttpServletResponse response) throws IOException {
        Item folder = item(owner, id);
        if (!folder.folder) throw new CustomException(400, "不是文件夹");
        String validator = ifRange();
        // A resumed folder download must keep its original immutable archive even if the folder changed.
        if (range != null && validator != null) {
            String previous = validator.replace("\"", "");
            String key = (owner == null ? "p-0-" : "u-" + owner + "-") + digest(List.of(id).toString());
            if (TOKEN.matcher(previous).matches() && previous.startsWith(key + "-")) {
                Path old = directory.resolve(previous + ".zip");
                boolean resumable;
                synchronized (monitor) {
                    resumable = Files.isRegularFile(old, LinkOption.NOFOLLOW_LINKS) && !expired(old);
                }
                if (resumable) { stream(owner, previous, folder.name + ".zip", range, response); return; }
            }
            range = null; // No matching representation: send a complete new ZIP rather than mismatched bytes.
        }
        String token = (String) create(owner, List.of(id)).get("taskId");
        stream(owner, token, folder.name + ".zip", range, response);
    }

    private String ifRange() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs
                ? attrs.getRequest().getHeader("If-Range") : null;
    }

    public void stream(Long owner, String token, String name, String range, HttpServletResponse response) throws IOException {
        String prefix = owner == null ? "p-0-" : "u-" + owner + "-";
        if (token == null || !TOKEN.matcher(token).matches() || !token.startsWith(prefix)) throw new CustomException(404, "下载任务不存在");
        Path path = directory.resolve(token + ".zip");
        synchronized (monitor) {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || (expired(path) && !active.containsKey(path))) throw new CustomException(410, "压缩包已过期，请重新打包");
            touch(path); active.merge(path, 1, Integer::sum);
        }
        try {
            if (range != null && ifRange() != null && !("\"" + token + "\"").equals(ifRange())) range = null;
            long size = Files.size(path), start = 0, end = size - 1;
            if (range != null) {
                try {
                    if (!range.startsWith("bytes=") || range.contains(",")) throw new IllegalArgumentException();
                    String[] parts = range.substring(6).split("-", -1);
                    if (parts.length != 2) throw new IllegalArgumentException();
                    if (parts[0].isEmpty()) { long suffix = Long.parseLong(parts[1]); if (suffix <= 0) throw new IllegalArgumentException(); start = Math.max(0, size - suffix); }
                    else { start = Long.parseLong(parts[0]); if (!parts[1].isEmpty()) end = Math.min(end, Long.parseLong(parts[1])); }
                    if (start < 0 || start > end || start >= size) throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) { response.setHeader("Content-Range", "bytes */" + size); response.sendError(416); return; }
            }
            response.setContentType("application/zip"); response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20"));
            response.setHeader("ETag", "\"" + token + "\"");
            response.setStatus(range == null ? 200 : 206);
            if (range != null) response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + size);
            response.setContentLengthLong(end - start + 1);
            try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
                file.seek(start); byte[] buffer = new byte[65536]; long remaining = end - start + 1;
                long expected = remaining;
                OutputStream out;
                try { out = response.getOutputStream(); }
                catch (IOException e) { throw DownloadTransferException.responseFailure(e, 0, expected); }
                while (remaining > 0) {
                    int count = file.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (count < 0) throw new EOFException("压缩包意外截断");
                    try { out.write(buffer, 0, count); }
                    catch (IOException e) { throw DownloadTransferException.responseFailure(e, expected - remaining, expected); }
                    remaining -= count;
                }
                try { out.flush(); }
                catch (IOException e) { throw DownloadTransferException.responseFailure(e, expected, expected); }
            }
        } finally {
            synchronized (monitor) {
                try { touch(path); } finally { active.computeIfPresent(path, (p, count) -> count == 1 ? null : count - 1); }
                // An older version in use is removed only once its last streaming request releases it.
                List<Path> versions = files().stream().filter(p -> p.getFileName().toString().startsWith(selectionKey(token) + "-")).toList();
                Optional<Path> newest = versions.stream().max(Comparator.comparingLong(this::created));
                if (newest.isPresent()) removeOlder(selectionKey(token), newest.get());
            }
        }
    }

    private String selectionKey(String token) { return String.join("-", Arrays.copyOf(token.split("-"), 3)); }
    private long created(Path path) { return Long.parseLong(path.getFileName().toString().split("-")[4]); }
    private boolean expired(Path path) throws IOException { return System.currentTimeMillis() - Files.getLastModifiedTime(path).toMillis() >= config.getRetentionHours() * 3600000L; }
    private void touch(Path path) throws IOException { Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis())); }
    private List<Path> files() throws IOException {
        if (!Files.isDirectory(directory)) return List.of();
        try (var paths = Files.list(directory)) { return paths.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS) && p.getFileName().toString().endsWith(".zip")
                && TOKEN.matcher(p.getFileName().toString().replaceFirst("\\.zip$", "")).matches()).toList(); }
    }
    private List<Path> allCacheFiles() throws IOException {
        try (var paths = Files.list(directory)) { return paths.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)).toList(); }
    }
    private void removeOlder(String key, Path newest) {
        try { for (Path path : files()) if (!path.equals(newest) && path.getFileName().toString().startsWith(key + "-") && !active.containsKey(path)) delete(path); }
        catch (IOException e) { log.warn("重复压缩包清理失败", e); }
    }
    private void delete(Path path) { try { Files.deleteIfExists(path); } catch (IOException e) { log.warn("临时压缩包清理失败，将稍后重试: {}", path, e); } }

    @Scheduled(fixedDelayString = "${download.archive.cleanup-interval-ms:300000}")
    public void cleanup() {
        synchronized (monitor) {
            try {
                if (!Files.isDirectory(directory)) return;
                for (Path path : allCacheFiles()) {
                    String name = path.getFileName().toString();
                    boolean owned = name.endsWith(".zip") || name.endsWith(".part");
                    if (owned && TOKEN.matcher(name.replaceFirst("\\.(zip|part)$", "")).matches() && !active.containsKey(path) && expired(path)) delete(path);
                }
                Map<String, Path> newest = new HashMap<>();
                for (Path path : files()) newest.merge(selectionKey(path.getFileName().toString()), path, (a, b) -> created(a) >= created(b) ? a : b);
                newest.forEach(this::removeOlder);
                // Old releases wrote flat temp/<task-id>.zip files. Never scan upload chunks or source files.
                try (var old = Files.list(directory.getParent())) {
                    for (Path path : old.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS) && LEGACY.matcher(p.getFileName().toString()).matches()).toList()) if (expired(path)) delete(path);
                }
            } catch (IOException e) { log.warn("扫描临时下载目录失败，下次重试", e); }
        }
    }
}
