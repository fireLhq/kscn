package top.kscn.service;

import jakarta.annotation.PreDestroy;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import top.kscn.config.RemoteFileProperties;
import top.kscn.exception.CustomException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.*;

/** All network-file requests use a validated DNS resolver, including every redirected connection. */
@Service
public class RemoteFileClient implements AutoCloseable {
    private final RemoteFileProperties config;
    private final CloseableHttpClient client;
    private final Semaphore slots;
    public RemoteFileClient(RemoteFileProperties config) {
        this.config = config; this.slots = new Semaphore(config.getMaxConcurrentTransfers());
        DnsResolver dns = new DnsResolver() {
            public InetAddress[] resolve(String host) throws UnknownHostException { return publicAddresses(InetAddress.getAllByName(host)); }
            public String resolveCanonicalHostname(String host) { return host; }
        };
        var manager = PoolingHttpClientConnectionManagerBuilder.create().setDnsResolver(dns)
                .setMaxConnTotal(config.getMaxConcurrentTransfers()).setMaxConnPerRoute(config.getMaxConcurrentTransfers())
                .setDefaultConnectionConfig(ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(config.getConnectTimeoutSeconds()))
                        .setSocketTimeout(Timeout.ofSeconds(config.getReadTimeoutSeconds())).build()).build();
        this.client = HttpClients.custom().setConnectionManager(manager).disableRedirectHandling().disableAutomaticRetries()
                .disableCookieManagement().disableContentCompression()
                .setDefaultRequestConfig(RequestConfig.custom().setResponseTimeout(Timeout.ofSeconds(config.getReadTimeoutSeconds()))
                        .setConnectionRequestTimeout(Timeout.ofSeconds(5)).build()).build();
    }

    public static URI validUri(String value) {
        try {
            URI uri = new URI(value);
            if (value.length() > 8192 || !Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getFragment() != null || uri.getHost().contains("%")
                    || (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443)) throw new IllegalArgumentException();
            return uri;
        } catch (Exception e) { throw new CustomException(400, "仅支持公开HTTP/HTTPS直链（80/443端口），不支持账号密码、片段或特殊地址"); }
    }

    public static InetAddress[] publicAddresses(InetAddress[] addresses) throws UnknownHostException {
        if (addresses.length == 0) throw new UnknownHostException("No addresses");
        for (InetAddress address : addresses) {
            byte[] b = address.getAddress();
            boolean blocked = address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isMulticastAddress();
            if (b.length == 4) {
                int a = b[0] & 255, c = b[1] & 255, d = b[2] & 255;
                blocked |= a == 0 || a == 127 || a >= 224 || a == 10 || (a == 100 && c >= 64 && c <= 127)
                        || (a == 169 && c == 254) || (a == 172 && c >= 16 && c <= 31)
                        || (a == 192 && (c == 168 || (c == 0 && (d == 0 || d == 2))))
                        || (a == 198 && (c == 18 || c == 19 || (c == 51 && d == 100))) || (a == 203 && c == 0 && d == 113)
                        || (a == 168 && c == 63 && d == 129 && (b[3] & 255) == 16);
            } else {
                // Only ordinary global-unicast IPv6; excludes mapped, ULA, NAT64 and transition ranges.
                blocked |= (b[0] & 0xe0) != 0x20 || ((b[0] & 255) == 0x20 && (b[1] & 255) == 0x02)
                        || ((b[0] & 255) == 0x20 && (b[1] & 255) == 0x01 && ((b[2] & 255) < 2 || ((b[2] & 255) == 0x0d && (b[3] & 255) == 0xb8)));
            }
            if (blocked) throw new UnknownHostException("Remote files may only connect to public addresses");
        }
        return addresses;
    }

    public record Probe(String name, long size, boolean sizeKnown, String contentType) {}
    public Probe probe(String url) throws IOException {
        try (Response response = open(url, "bytes=0-4095")) {
            return describe(response, response.body().readNBytes(4096));
        }
    }
    public Probe describe(Response response, byte[] sample) {
            response.requireSuccess();
            if (sample.length == 0 && response.totalSize() != 0) throw new CustomException(400, "链接未返回可下载内容");
            String type = response.header("Content-Type").toLowerCase(Locale.ROOT);
            String disposition = response.header("Content-Disposition");
            String prefix = new String(sample, StandardCharsets.UTF_8).stripLeading().toLowerCase(Locale.ROOT);
            boolean attachment = disposition.toLowerCase(Locale.ROOT).contains("attachment");
            if (!attachment && (type.contains("text/html") || type.contains("application/xhtml") || prefix.startsWith("<!doctype html") || prefix.startsWith("<html")))
                throw new CustomException(400, "链接返回的是网页或登录页面，请提供无需登录的文件直链");
            String path = response.uri.getPath();
            if (!attachment && (type.contains("json") || type.contains("xml")) && !path.matches("(?i).*\\.(json|xml|svg|rss|atom)$"))
                throw new CustomException(400, "链接可能是接口或错误响应，请使用文件直链");
            String name = null;
            try { if (!disposition.isBlank()) name = ContentDisposition.parse(disposition).getFilename(); } catch (IllegalArgumentException ignored) {}
            if (name == null || name.isBlank()) name = path.substring(path.lastIndexOf('/') + 1);
            if (name.isBlank()) name = "download.bin";
            FileOperationService.validName(name);
            return new Probe(name, Math.max(0, response.totalSize()), response.totalSize() >= 0, type.split(";")[0]);
    }

    public Response open(String url, String range) throws IOException {
        return open(url, range, null);
    }
    public Response open(String url, String range, String ifRange) throws IOException {
        URI uri = validUri(url);
        if (range != null && !range.matches("bytes=(?:[0-9]+-[0-9]*|-[0-9]+)")) throw new CustomException(416, "仅支持单段Range请求");
        if (ifRange != null && (ifRange.length() > 512 || ifRange.indexOf('\r') >= 0 || ifRange.indexOf('\n') >= 0)) throw new CustomException(400, "无效的If-Range");
        if (!slots.tryAcquire()) throw new CustomException(429, "网络文件处理繁忙，请稍后重试");
        try {
            for (int redirect = 0; redirect <= config.getMaxRedirects(); redirect++) {
                // Validate on every request, not just insertion. The same resolver pins the actual socket address.
                publicAddresses(InetAddress.getAllByName(uri.getHost()));
                HttpGet get = new HttpGet(uri); get.setHeader("Accept-Encoding", "identity");
                if (range != null) get.setHeader("Range", range);
                if (range != null && ifRange != null) get.setHeader("If-Range", ifRange);
                CloseableHttpResponse response = client.execute(get);
                int code = response.getCode();
                if (Set.of(301, 302, 303, 307, 308).contains(code)) {
                    String location = response.getFirstHeader("Location") == null ? null : response.getFirstHeader("Location").getValue();
                    response.close(org.apache.hc.core5.io.CloseMode.IMMEDIATE);
                    if (location == null) throw new CustomException(400, "链接重定向无效");
                    uri = validUri(uri.resolve(location).toString()); continue;
                }
                String encoding = response.getFirstHeader("Content-Encoding") == null ? "identity" : response.getFirstHeader("Content-Encoding").getValue();
                if (!encoding.equalsIgnoreCase("identity")) { response.close(org.apache.hc.core5.io.CloseMode.IMMEDIATE); throw new CustomException(400, "直链返回传输压缩内容，暂不支持"); }
                return new Response(uri, response, slots, config.getMaxDurationSeconds());
            }
            throw new CustomException(400, "链接重定向次数过多");
        } catch (IOException | RuntimeException e) { slots.release(); throw e; }
    }

    public static class Response implements AutoCloseable {
        final URI uri;
        private final CloseableHttpResponse response;
        private final Semaphore slots;
        private final long deadline;
        private boolean closed;
        Response(URI uri, CloseableHttpResponse response, Semaphore slots, int seconds) {
            this.uri = uri; this.response = response; this.slots = slots; this.deadline = System.nanoTime() + seconds * 1000000000L;
        }
        public int status() { return response.getCode(); }
        public String header(String name) { return response.getFirstHeader(name) == null ? "" : response.getFirstHeader(name).getValue(); }
        public long totalSize() {
            if (status() == 206) {
                Matcher match = Pattern.compile("bytes [0-9]+-[0-9]+/([0-9]+)").matcher(header("Content-Range"));
                if (match.matches()) try { return Long.parseLong(match.group(1)); } catch (NumberFormatException ignored) {}
                return -1;
            }
            try { return Long.parseLong(header("Content-Length")); } catch (NumberFormatException e) { return -1; }
        }
        public void requireSuccess() { if (status() != 200 && status() != 206) throw new CustomException(400, "远程链接不可下载（HTTP " + status() + "），可能已失效或需要登录"); }
        public InputStream body() throws IOException {
            if (response.getEntity() == null) return InputStream.nullInputStream();
            return new FilterInputStream(response.getEntity().getContent()) {
                private void check() throws SocketTimeoutException { if (System.nanoTime() > deadline) throw new SocketTimeoutException("Remote transfer duration exceeded"); }
                public int read() throws IOException { check(); return in.read(); }
                public int read(byte[] data, int off, int length) throws IOException { check(); return in.read(data, off, length); }
            };
        }
        public synchronized void close() {
            if (closed) return;
            closed = true;
            try { response.close(org.apache.hc.core5.io.CloseMode.IMMEDIATE); } finally { slots.release(); }
        }
    }
    @PreDestroy public void close() throws IOException { client.close(); }
}
