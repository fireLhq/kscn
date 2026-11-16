package top.kscn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.kscn.entity.FileStorage;
import top.kscn.mapper.FileStorageMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageGarbageCollector {
    private final FileStorageMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void collect(Long storageId) {
        FileStorage storage = mapper.lockById(storageId);
        if (storage == null || storage.getStatus() != 0) return;
        int references = mapper.countActualReferences(storageId);
        if (references != 0) {
            mapper.setReferences(storageId, references, 1);
            return;
        }
        try {
            if (!storage.isRemote()) Files.deleteIfExists(Paths.get(storage.getFilePath()));
            mapper.deleteIfNoRef(storageId);
        } catch (IOException exception) {
            // 保留待清理记录，下次重试，不把失败误报为已删除。
            log.warn("源文件清理失败，稍后重试: {}", storageId, exception);
        }
    }
}
