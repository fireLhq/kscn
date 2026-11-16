package top.kscn.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.kscn.config.RemoteFileProperties;
import top.kscn.entity.FileStorage;
import top.kscn.exception.CustomException;
import top.kscn.exception.DownloadTransferException;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service @RequiredArgsConstructor
public class RemoteFileAccess {
    private final RemoteFileClient client;
    private final RemoteFileProperties config;

    public byte[] preview(FileStorage file) {
        int limit = FilePropertiesService.PREVIEW_LIMIT;
        try (var response = client.open(file.getRemoteUrl(), null)) {
            response.requireSuccess();
            if (response.status() != 200) throw new CustomException(400, "远程服务器未返回完整文件");
            if (response.totalSize() >= limit) throw new CustomException(413, "远程文件过大，仅支持小于10MB的预览");
            InputStream input = response.body(); byte[] sample = input.readNBytes(4096); client.describe(response, sample);
            ByteArrayOutputStream output = new ByteArrayOutputStream(); output.write(sample);
            byte[] data = new byte[65536]; int count;
            while ((count = input.read(data)) != -1) {
                if (count >= limit - output.size()) throw new CustomException(413, "远程文件实际内容超过预览上限");
                output.write(data, 0, count);
            }
            if (response.totalSize() >= 0 && response.totalSize() != output.size()) throw new CustomException(409, "远程文件传输不完整，请重试");
            return output.toByteArray();
        } catch (IOException e) { throw new CustomException(400, "远程文件无法读取，链接可能已失效或网络异常"); }
    }

    public void download(FileStorage file, String name, String range, HttpServletResponse target) throws IOException {
        String validator = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
                instanceof org.springframework.web.context.request.ServletRequestAttributes attrs ? attrs.getRequest().getHeader("If-Range") : null;
        try (var response = client.open(file.getRemoteUrl(), range, validator)) {
            if (response.status() == 416) { target.sendError(416); return; }
            response.requireSuccess();
            if (range == null && response.status() != 200) throw new CustomException(400, "远程服务器未返回完整文件");
            long limit = config.getMaxDownloadSize().toBytes();
            if (response.totalSize() > limit) throw new CustomException(413, "远程文件超过单次下载上限");
            InputStream input = response.body(); byte[] sample = input.readNBytes(4096); client.describe(response, sample);
            if (response.status() == 206 && !response.header("Content-Range").matches("bytes [0-9]+-[0-9]+/[0-9]+"))
                throw new CustomException(400, "远程服务器返回了无效的分段响应");
            target.setStatus(response.status()); target.setContentType("application/octet-stream");
            target.setHeader("Cache-Control", "no-store"); target.setHeader("X-Content-Type-Options", "nosniff");
            target.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20"));
            for (String header : new String[]{"Content-Range", "Accept-Ranges", "ETag", "Last-Modified"})
                if (!response.header(header).isBlank()) target.setHeader(header, response.header(header));
            // Do not claim range support where the upstream server does not provide it.
            long expected = -1;
            try { expected = Long.parseLong(response.header("Content-Length")); } catch (NumberFormatException ignored) {}
            if (expected >= 0) target.setContentLengthLong(expected);
            long written = 0; OutputStream output = target.getOutputStream(); byte[] data = sample;
            while (data.length > 0) {
                if (data.length > limit - written) throw new IOException("Remote response exceeded download limit");
                try { output.write(data); } catch (IOException e) { throw DownloadTransferException.responseFailure(e, written, expected); }
                written += data.length; data = input.readNBytes(65536);
            }
            if (expected >= 0 && expected != written) throw new IOException("Remote response truncated");
            try { output.flush(); } catch (IOException e) { throw DownloadTransferException.responseFailure(e, written, expected); }
        }
    }
}
