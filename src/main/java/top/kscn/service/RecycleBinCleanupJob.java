package top.kscn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.kscn.mapper.FileStorageMapper;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecycleBinCleanupJob {
    private final JdbcTemplate jdbc;
    private final RecycleBinService recycleBin;
    private final FileStorageMapper storageMapper;
    private final StorageGarbageCollector collector;

    // 每分钟分批处理；过期项目立即禁止恢复，清理失败保留数据库记录以供重试。
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void clean() {
        for (String table : new String[]{"public_file", "user_file"}) {
            for (Map<String, Object> row : jdbc.queryForList("SELECT * FROM " + table
                    + " WHERE is_deleted = 1 AND delete_time <= DATE_SUB(NOW(), INTERVAL 7 DAY) ORDER BY delete_time, file_id LIMIT 200")) {
                Long userId = row.get("user_id") == null ? null : ((Number) row.get("user_id")).longValue();
                Long fileId = ((Number) row.get("file_id")).longValue();
                try {
                    recycleBin.purgeExpired(userId, fileId);
                } catch (Exception exception) {
                    log.warn("回收站过期清理失败: {} / {}", table, fileId, exception);
                }
            }
        }
        for (Long storageId : storageMapper.selectPendingDeletion()) {
            try {
                collector.collect(storageId);
            } catch (Exception exception) {
                log.warn("物理存储清理失败: {}", storageId, exception);
            }
        }
    }
}
