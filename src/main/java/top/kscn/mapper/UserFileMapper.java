package top.kscn.mapper;

import org.apache.ibatis.annotations.*;
import top.kscn.entity.UserFile;

import java.util.List;

/**
 * 用户文件系统Mapper
 */
@Mapper
public interface UserFileMapper {

    /**
     * 根据ID查询文件
     */
    @Select("SELECT * FROM user_file WHERE file_id = #{fileId} AND is_deleted = 0")
    UserFile selectById(@Param("fileId") Long fileId);

    /**
     * 查询用户指定目录下的文件列表
     */
    @Select("SELECT * FROM user_file WHERE user_id = #{userId} AND parent_id <=> #{parentId} AND is_deleted = 0 ORDER BY is_folder DESC, file_name ASC")
    List<UserFile> selectByUserAndParent(@Param("userId") Long userId, @Param("parentId") Long parentId);

    /**
     * 根据用户ID和文件路径查询文件
     */
    @Select("SELECT * FROM user_file WHERE user_id = #{userId} AND parent_id <=> #{parentId} AND file_name = #{fileName} AND is_deleted = 0")
    UserFile selectByUserAndPath(@Param("userId") Long userId, @Param("parentId") Long parentId, @Param("fileName") String fileName);

    /**
     * 检查文件名是否存在
     */
    @Select("SELECT COUNT(*) FROM user_file WHERE user_id = #{userId} AND parent_id <=> #{parentId} AND file_name = #{fileName} AND is_deleted = 0")
    int countByFileName(@Param("userId") Long userId, @Param("parentId") Long parentId, @Param("fileName") String fileName);

    /**
     * 插入文件记录
     */
    @Insert("INSERT INTO user_file (user_id, parent_id, file_name, is_folder, storage_id, file_size, file_type, is_deleted, create_time, update_time) " +
            "VALUES (#{userId}, #{parentId}, #{fileName}, #{isFolder}, #{storageId}, #{fileSize}, #{fileType}, #{isDeleted}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "fileId")
    int insert(UserFile userFile);

    /**
     * 更新文件名
     */
    @Update("UPDATE user_file SET file_name = #{fileName}, update_time = NOW() WHERE file_id = #{fileId}")
    int updateFileName(@Param("fileId") Long fileId, @Param("fileName") String fileName);

    /**
     * 更新父目录
     */
    @Update("UPDATE user_file SET parent_id = #{parentId}, update_time = NOW() WHERE file_id = #{fileId}")
    int updateParentId(@Param("fileId") Long fileId, @Param("parentId") Long parentId);

    /**
     * 逻辑删除文件
     */
    @Update("UPDATE user_file SET is_deleted = 1, update_time = NOW() WHERE file_id = #{fileId}")
    int logicDelete(@Param("fileId") Long fileId);

    /**
     * 物理删除文件
     */
    @Delete("DELETE FROM user_file WHERE file_id = #{fileId}")
    int physicalDelete(@Param("fileId") Long fileId);

    /**
     * 查询文件夹下的所有子文件（递归查询用）
     */
    @Select("SELECT * FROM user_file WHERE parent_id = #{parentId} AND is_deleted = 0")
    List<UserFile> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 搜索文件
     */
    @Select("SELECT * FROM user_file WHERE user_id = #{userId} AND file_name LIKE CONCAT('%', #{keyword}, '%') AND is_deleted = 0 ORDER BY is_folder DESC, file_name ASC")
    List<UserFile> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 查询用户的回收站文件
     */
    @Select("SELECT * FROM user_file WHERE user_id = #{userId} AND is_deleted = 1 ORDER BY update_time DESC")
    List<UserFile> selectDeletedFiles(@Param("userId") Long userId);

    /**
     * 恢复文件
     */
    @Update("UPDATE user_file SET is_deleted = 0, update_time = NOW() WHERE file_id = #{fileId}")
    int restore(@Param("fileId") Long fileId);

    /**
     * 查询用户所有文件夹（构建树形结构用）
     */
    @Select("SELECT * FROM user_file WHERE user_id = #{userId} AND is_folder = 1 AND is_deleted = 0 ORDER BY file_name ASC")
    List<UserFile> selectAllFolders(@Param("userId") Long userId);
    
    /**
     * 计算用户所有文件的总大小（包括回收站，不包括文件夹）
     * 注意：这里统计的是数据库记录的大小总和，不是物理文件大小
     * 即使多个文件指向同一个物理文件，也会分别计算
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM user_file WHERE user_id = #{userId} AND is_folder = 0")
    Long calculateUserTotalFileSize(@Param("userId") Long userId);
}

