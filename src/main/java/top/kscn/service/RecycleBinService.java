package top.kscn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.kscn.exception.CustomException;
import top.kscn.utils.RestoreFileName;

import java.sql.Timestamp;
import java.util.*;

/** 公共和个人文件共用回收站逻辑。userId 为 null 时表示公共文件。 */
@Service
@RequiredArgsConstructor
public class RecycleBinService {
    private final JdbcTemplate jdbc;
    private final FileStorageService storageService;

    private String table(Long userId) { return userId == null ? "public_file" : "user_file"; }
    private String scope(Long userId) { return userId == null ? "" : " AND user_id = " + userId; }

    // 事务行锁使同一空间内的删除/恢复/清理串行，避免两个恢复请求选中相同名称。
    private void lock(Long userId) {
        String key = userId == null ? "public" : "user:" + userId;
        jdbc.update("INSERT IGNORE INTO file_namespace_lock (scope_key) VALUES (?)", key);
        jdbc.queryForObject("SELECT scope_key FROM file_namespace_lock WHERE scope_key = ? FOR UPDATE", String.class, key);
    }

    @Transactional
    public void lockForWrite(Long userId, Long parentId) {
        lock(userId);
        if (parentId != null && !usableParent(userId, parentId))
            throw new CustomException(404, "目标目录不存在、已删除或无权访问");
    }

    public void checkMove(Long userId, Long fileId, Long targetParentId) {
        Set<Long> seen = new HashSet<>();
        while (targetParentId != null) {
            if (targetParentId.equals(fileId) || !seen.add(targetParentId))
                throw new CustomException(400, "不能将文件夹移入自身或其子目录");
            Object parent = find(userId, targetParentId).get("parent_id");
            targetParentId = parent == null ? null : ((Number) parent).longValue();
        }
    }

    private Map<String, Object> find(Long userId, Long id) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM " + table(userId)
                + " WHERE file_id = ?" + scope(userId) + " FOR UPDATE", id);
        if (rows.isEmpty()) throw new CustomException(404, "文件不存在或无权操作");
        return rows.get(0);
    }

    private long id(Map<String, Object> row) { return ((Number) row.get("file_id")).longValue(); }
    private boolean flag(Map<String, Object> row, String field) {
        Object value = row.get(field);
        return value instanceof Boolean ? (Boolean) value : ((Number) value).intValue() == 1;
    }
    private List<Map<String, Object>> children(Long userId, long parent) {
        return jdbc.queryForList("SELECT * FROM " + table(userId) + " WHERE parent_id = ?" + scope(userId)
                + " ORDER BY file_id FOR UPDATE", parent);
    }

    @Transactional
    public void delete(Long userId, Long fileId) {
        lock(userId);
        Map<String, Object> file = find(userId, fileId);
        if (flag(file, "is_deleted")) throw new CustomException(400, "文件已在回收站");
        markDeleted(userId, file, UUID.randomUUID().toString(), new Timestamp(System.currentTimeMillis()));
    }

    private void markDeleted(Long userId, Map<String, Object> file, String batch, Timestamp time) {
        if (flag(file, "is_deleted")) return; // 之前单独删除的项目保留其删除时间和批次。
        if (flag(file, "is_folder")) {
            for (Map<String, Object> child : children(userId, id(file))) markDeleted(userId, child, batch, time);
        }
        jdbc.update("UPDATE " + table(userId) + " SET is_deleted = 1, delete_time = ?, delete_batch = ?, update_time = NOW() WHERE file_id = ?",
                time, batch, id(file));
    }

    @Transactional
    public void restore(Long userId, Long fileId) {
        lock(userId);
        Map<String, Object> file = find(userId, fileId);
        if (!flag(file, "is_deleted")) throw new CustomException(400, "文件不在回收站");
        Long parent = file.get("parent_id") == null ? null : ((Number) file.get("parent_id")).longValue();
        if (parent != null && !usableParent(userId, parent)) parent = recoveryFolder(userId);
        restoreTree(userId, file, parent, file.get("delete_batch"));
    }

    private boolean usableParent(Long userId, Long parent) {
        Set<Long> seen = new HashSet<>();
        while (parent != null) {
            if (!seen.add(parent)) return false;
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM " + table(userId)
                    + " WHERE file_id = ? AND is_folder = 1 AND is_deleted = 0" + scope(userId), parent);
            if (rows.isEmpty()) return false;
            Object next = rows.get(0).get("parent_id");
            parent = next == null ? null : ((Number) next).longValue();
        }
        return true;
    }

    private boolean nameExists(Long userId, Long parent, String name) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table(userId)
                + " WHERE parent_id <=> ? AND file_name = ? AND is_deleted = 0" + scope(userId), Long.class, parent, name) > 0;
    }

    private Long recoveryFolder(Long userId) {
        // 同名普通文件不作为恢复目录；自动为恢复目录选择可用名称。
        List<Long> existing = jdbc.queryForList("SELECT file_id FROM " + table(userId)
                + " WHERE parent_id IS NULL AND file_name = '恢复文件夹' AND is_folder = 1 AND is_deleted = 0"
                + scope(userId) + " ORDER BY file_id LIMIT 1", Long.class);
        if (!existing.isEmpty()) return existing.get(0);
        String name = RestoreFileName.available("恢复文件夹", true, candidate -> {
            List<Map<String, Object>> matches = jdbc.queryForList("SELECT * FROM " + table(userId)
                    + " WHERE parent_id IS NULL AND file_name = ? AND is_deleted = 0" + scope(userId), candidate);
            return !matches.isEmpty() && matches.stream().noneMatch(item -> flag(item, "is_folder"));
        });
        List<Long> reusable = jdbc.queryForList("SELECT file_id FROM " + table(userId)
                + " WHERE parent_id IS NULL AND file_name = ? AND is_folder = 1 AND is_deleted = 0"
                + scope(userId) + " ORDER BY file_id LIMIT 1", Long.class, name);
        if (!reusable.isEmpty()) return reusable.get(0);
        if (userId == null) {
            jdbc.update("INSERT INTO public_file (file_name, is_folder) VALUES (?, 1)", name);
        } else {
            jdbc.update("INSERT INTO user_file (user_id, file_name, is_folder) VALUES (?, ?, 1)", userId, name);
        }
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    private void restoreTree(Long userId, Map<String, Object> file, Long parent, Object batch) {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM " + table(userId)
                + " WHERE file_id = ? AND delete_time > DATE_SUB(NOW(), INTERVAL 7 DAY)", Long.class, id(file)) == 0)
            throw new CustomException(410, "文件已超过7天保留期，无法恢复");
        String name = RestoreFileName.available((String) file.get("file_name"), flag(file, "is_folder"),
                candidate -> nameExists(userId, parent, candidate));
        jdbc.update("UPDATE " + table(userId) + " SET parent_id = ?, file_name = ?, is_deleted = 0, delete_time = NULL, delete_batch = NULL, update_time = NOW() WHERE file_id = ?",
                parent, name, id(file));
        if (flag(file, "is_folder")) {
            for (Map<String, Object> child : children(userId, id(file))) {
                if (flag(child, "is_deleted") && Objects.equals(batch, child.get("delete_batch")))
                    restoreTree(userId, child, id(file), batch);
            }
        }
    }

    @Transactional
    public void purge(Long userId, Long fileId) {
        lock(userId);
        purgeTree(userId, find(userId, fileId));
        if (userId != null) {
            jdbc.update("UPDATE user SET used_space = (SELECT COALESCE(SUM(file_size), 0) FROM user_file WHERE user_id = ? AND is_folder = 0) WHERE user_id = ?", userId, userId);
        }
    }

    @Transactional
    public void purgeExpired(Long userId, Long fileId) {
        lock(userId);
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM " + table(userId)
                + " WHERE file_id = ? AND is_deleted = 1 AND delete_time <= DATE_SUB(NOW(), INTERVAL 7 DAY)"
                + scope(userId) + " FOR UPDATE", fileId);
        // 可能已经被前面的父目录清理，或在扫描后被恢复/再次删除。
        if (!rows.isEmpty()) purge(userId, fileId);
    }

    @Transactional
    public void empty(Long userId) {
        lock(userId);
        List<Long> ids = jdbc.queryForList("SELECT file_id FROM " + table(userId)
                + " WHERE is_deleted = 1" + scope(userId) + " ORDER BY file_id", Long.class);
        for (Long fileId : ids) {
            if (jdbc.queryForObject("SELECT COUNT(*) FROM " + table(userId) + " WHERE file_id = ?" + scope(userId), Long.class, fileId) > 0)
                purge(userId, fileId);
        }
    }

    private void purgeTree(Long userId, Map<String, Object> file) {
        if (!flag(file, "is_deleted")) throw new CustomException(400, "仅可永久删除回收站中的文件");
        if (flag(file, "is_folder")) {
            for (Map<String, Object> child : children(userId, id(file))) purgeTree(userId, child);
        }
        jdbc.update("DELETE FROM " + table(userId) + " WHERE file_id = ?", id(file));
        if (file.get("storage_id") != null)
            storageService.decrementRefCount(((Number) file.get("storage_id")).longValue());
    }
}
