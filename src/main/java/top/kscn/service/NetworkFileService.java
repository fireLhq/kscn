package top.kscn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.config.RemoteFileProperties;
import top.kscn.entity.dto.NetworkFileRequest;
import top.kscn.exception.CustomException;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

@Service @RequiredArgsConstructor
public class NetworkFileService {
    private final RemoteFileClient client;
    private final RemoteFileProperties config;
    private final NetworkFileWriter writer;
    private final UserFileService userFiles;
    private final PublicFileService publicFiles;
    @Value("${file.storage-root}") private String storageRoot;

    public RemoteFileClient.Probe check(String url) {
        try { return client.probe(url); }
        catch (IOException e) { throw new CustomException(400, "无法访问直链，请检查链接、有效期及访问限制"); }
    }

    public Map<String, Object> create(Long userId, NetworkFileRequest request) {
        RemoteFileClient.validUri(request.getUrl());
        writer.checkTarget(userId, request.getParentId());
        if (!Set.of("IMPORT", "LINK").contains(request.getMode())) throw new CustomException(400, "请选择网络导入或远程链接");
        Path temporary = null;
        try {
            if ("LINK".equals(request.getMode())) {
                var probe = client.probe(request.getUrl()); String name = name(request, probe);
                if (probe.sizeKnown() && probe.size() > config.getMaxDownloadSize().toBytes()) throw new CustomException(413, "远程文件超过下载上限");
                return Map.of("fileId", writer.addLink(userId, request.getParentId(), name, request.getUrl(), probe), "mode", "LINK");
            }
            try (var response = client.open(request.getUrl(), null)) {
                response.requireSuccess();
                if (response.status() != 200) throw new CustomException(400, "远程服务器未返回完整文件");
                InputStream input = response.body(); byte[] first = input.readNBytes(4096);
                var probe = client.describe(response, first); String name = name(request, probe);
                if (probe.sizeKnown() && probe.size() > config.getMaxImportSize().toBytes()) throw new CustomException(413, "网络导入文件超过大小上限");
                Path folder = Paths.get(storageRoot, ".network-import"); Files.createDirectories(folder);
                temporary = Files.createTempFile(folder, "net-", ".part");
                MessageDigest digest = MessageDigest.getInstance("MD5"); long count = 0;
                try (OutputStream output = Files.newOutputStream(temporary)) {
                    byte[] data = first;
                    while (data.length != 0) {
                        if (data.length > config.getMaxImportSize().toBytes() - count) throw new CustomException(413, "实际下载内容超过网络导入上限");
                        output.write(data); digest.update(data); count += data.length;
                        data = input.readNBytes(65536);
                    }
                }
                if (probe.sizeKnown() && probe.size() != count) throw new CustomException(409, "网络文件传输不完整，请重试");
                var file = new DiskFile(temporary, name, probe.contentType());
                String md5 = HexFormat.of().formatHex(digest.digest());
                Long id = userId == null ? publicFiles.uploadFile(request.getParentId(), file, md5, null).getFileId()
                        : userFiles.uploadFile(userId, request.getParentId(), file, md5).getFileId();
                return Map.of("fileId", id, "mode", "IMPORT");
            }
        } catch (IOException e) { throw new CustomException(400, "网络文件获取失败，请检查直链或稍后重试"); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        finally { if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) {} }
    }
    private String name(NetworkFileRequest request, RemoteFileClient.Probe probe) {
        String name = request.getFileName() == null || request.getFileName().isBlank() ? probe.name() : request.getFileName();
        FileOperationService.validName(name); NetworkFileWriter.extension(name); return name;
    }

    private record DiskFile(Path path, String originalFilename, String contentType) implements MultipartFile {
        public String getName() { return "file"; }
        public String getOriginalFilename() { return originalFilename; }
        public String getContentType() { return contentType; }
        public boolean isEmpty() { return getSize() == 0; }
        public long getSize() { try { return Files.size(path); } catch (IOException e) { throw new UncheckedIOException(e); } }
        public byte[] getBytes() throws IOException { return Files.readAllBytes(path); }
        public InputStream getInputStream() throws IOException { return Files.newInputStream(path); }
        public void transferTo(File destination) throws IOException { Files.copy(path, destination.toPath(), StandardCopyOption.REPLACE_EXISTING); }
    }
    @Scheduled(fixedDelay = 900000, initialDelay = 60000)
    public void cleanupImports() {
        Path folder = Paths.get(storageRoot, ".network-import");
        if (!Files.isDirectory(folder)) return;
        try (var paths = Files.list(folder)) {
            for (Path path : paths.filter(p -> p.getFileName().toString().matches("net-.*\\.part") && Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)).toList()) {
                try { if (System.currentTimeMillis() - Files.getLastModifiedTime(path).toMillis() > 86400000) Files.deleteIfExists(path); }
                catch (IOException ignored) {}
            }
        } catch (IOException ignored) {}
    }
}
