package top.kscn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.kscn.entity.FileStorage;
import top.kscn.exception.CustomException;
import top.kscn.mapper.FileStorageMapper;

/** Remote logical size is zero: remote bytes are not charged as local storage, including copies/recycle. */
@Service @RequiredArgsConstructor
public class NetworkFileWriter {
    private final JdbcTemplate jdbc;
    private final RecycleBinService namespace;
    private final FileStorageMapper storage;

    @Transactional
    public void checkTarget(Long userId, Long parent) { namespace.lockForWrite(userId, parent); }

    @Transactional
    public Long addLink(Long userId, Long parent, String name, String url, RemoteFileClient.Probe probe) {
        namespace.lockForWrite(userId, parent); FileOperationService.validName(name);
        String table = userId == null ? "public_file" : "user_file";
        Integer duplicate = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE parent_id <=> ? AND file_name = ? AND is_deleted = 0"
                + (userId == null ? "" : " AND user_id = " + userId), Integer.class, parent, name);
        if (duplicate != null && duplicate > 0) throw new CustomException(409, "目标目录已有同名项目，请修改名称");
        FileStorage remote = new FileStorage(); remote.setSourceType("REMOTE"); remote.setRemoteUrl(url);
        remote.setFileSize(probe.size()); remote.setSizeKnown(probe.sizeKnown());
        storage.insertRemote(remote);
        String extension = extension(name);
        if (userId == null) jdbc.update("INSERT INTO public_file (parent_id,file_name,is_folder,storage_id,file_size,file_type) VALUES (?,?,0,?,0,?)", parent, name, remote.getStorageId(), extension);
        else jdbc.update("INSERT INTO user_file (user_id,parent_id,file_name,is_folder,storage_id,file_size,file_type) VALUES (?,?,?,0,?,0,?)", userId, parent, name, remote.getStorageId(), extension);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
    static String extension(String name) {
        int dot = name.lastIndexOf('.'); String extension = dot > 0 ? name.substring(dot + 1).toLowerCase(java.util.Locale.ROOT) : "";
        if (extension.length() > 50) throw new CustomException(400, "扩展名过长");
        return extension;
    }
}
