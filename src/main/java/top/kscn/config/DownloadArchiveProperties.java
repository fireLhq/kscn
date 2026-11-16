package top.kscn.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Data
@Component
@ConfigurationProperties(prefix = "download.archive")
public class DownloadArchiveProperties {
    private int retentionHours = 24;
    private long cleanupIntervalMs = 300000;
    private DataSize maxSourceSize = DataSize.ofGigabytes(1);
    private DataSize maxArchiveSize = DataSize.ofGigabytes(1);
    private DataSize maxCacheSize = DataSize.ofGigabytes(10);
    private int maxEntries = 10000;
    private int maxConcurrentBuilds = 2;

    @PostConstruct
    public void validate() {
        if (retentionHours < 1 || cleanupIntervalMs < 1 || maxSourceSize.toBytes() < 1 || maxArchiveSize.toBytes() < 1
                || maxCacheSize.toBytes() < maxArchiveSize.toBytes() || maxEntries < 1 || maxConcurrentBuilds < 1)
            throw new IllegalArgumentException("download.archive 配置无效：大小与数量须为正，缓存容量不能小于单包上限");
    }
}
