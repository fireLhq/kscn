package top.kscn.mapper;

import org.apache.ibatis.annotations.*;
import top.kscn.entity.FileStorage;

/**
 * 物理文件存储Mapper
 */
@Mapper
public interface FileStorageMapper {

    /**
     * 根据MD5查询物理文件
     */
    @Select("SELECT * FROM file_storage WHERE file_md5 = #{fileMd5} AND status = 1")
    FileStorage selectByMd5(@Param("fileMd5") String fileMd5);

    /**
     * 根据ID查询物理文件
     */
    @Select("SELECT * FROM file_storage WHERE storage_id = #{storageId} AND status = 1")
    FileStorage selectById(@Param("storageId") Long storageId);

    /**
     * 插入物理文件记录
     */
    @Insert("INSERT INTO file_storage (file_md5, file_size, file_path, ref_count, status, create_time) " +
            "VALUES (#{fileMd5}, #{fileSize}, #{filePath}, #{refCount}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "storageId")
    int insert(FileStorage fileStorage);

    /**
     * 增加引用计数
     */
    @Update("UPDATE file_storage SET ref_count = ref_count + 1 WHERE storage_id = #{storageId}")
    int incrementRefCount(@Param("storageId") Long storageId);

    /**
     * 减少引用计数
     */
    @Update("UPDATE file_storage SET ref_count = ref_count - 1 WHERE storage_id = #{storageId}")
    int decrementRefCount(@Param("storageId") Long storageId);

    /**
     * 删除引用计数为0的物理文件记录
     */
    @Delete("DELETE FROM file_storage WHERE storage_id = #{storageId} AND ref_count = 0")
    int deleteIfNoRef(@Param("storageId") Long storageId);

    /**
     * 更新状态
     */
    @Update("UPDATE file_storage SET status = #{status} WHERE storage_id = #{storageId}")
    int updateStatus(@Param("storageId") Long storageId, @Param("status") Integer status);
}

