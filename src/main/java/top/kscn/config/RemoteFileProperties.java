package top.kscn.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Data @Component @ConfigurationProperties(prefix = "remote-file")
public class RemoteFileProperties {
    private DataSize maxImportSize = DataSize.ofMegabytes(500);
    private DataSize maxDownloadSize = DataSize.ofGigabytes(2);
    private int maxConcurrentTransfers = 4;
    private int connectTimeoutSeconds = 10;
    private int readTimeoutSeconds = 30;
    private int maxRedirects = 5;
    private int maxDurationSeconds = 600;
    @PostConstruct public void validate() {
        if (maxImportSize.toBytes() < 1 || maxDownloadSize.toBytes() < 1 || maxConcurrentTransfers < 1
                || connectTimeoutSeconds < 1 || readTimeoutSeconds < 1 || maxRedirects < 0 || maxDurationSeconds < 1)
            throw new IllegalArgumentException("remote-file 配置必须使用有效的正数上限");
    }
}
