package top.kscn.mapper;

import org.apache.ibatis.annotations.*;
import top.kscn.entity.PublicFile;

import java.util.List;

/**
 * 公共文件系统Mapper
 * 支持文件夹树形结构
 */
@Mapper
public interface PublicFileMapper {

    // ==================== 基础查询 ====================
    
    /**
     * 根据ID查询公共文件
     */
    @Select("SELECT * FROM public_file WHERE file_id = #{fileId}")
    PublicFile selectById(@Param("fileId") Long fileId);

    /**
     * 根据父ID查询文件列表（不包括已删除）
     */
    @Select("SELECT * FROM public_file WHERE parent_id <=> #{parentId} AND is_deleted = 0 ORDER BY is_folder DESC, file_name ASC")
    List<PublicFile> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据父ID和文件名查询（检查重名）
     */
    @Select("SELECT * FROM public_file WHERE parent_id <=> #{parentId} AND file_name = #{fileName} AND is_deleted = 0")
    PublicFile selectByParentIdAndName(@Param("parentId") Long parentId, @Param("fileName") String fileName);

    /**
     * 查询所有文件（不包括已删除）
     */
    @Select("SELECT * FROM public_file WHERE is_deleted = 0 ORDER BY create_time DESC")
    List<PublicFile> selectAll();

    /**
     * 查询所有文件夹（不包括已删除）
     */
    @Select("SELECT * FROM public_file WHERE is_folder = 1 AND is_deleted = 0 ORDER BY parent_id, file_name ASC")
    List<PublicFile> selectAllFolders();

    /**
     * 根据分类查询公共文件（不包括已删除）
     */
    @Select("SELECT * FROM public_file WHERE category = #{category} AND is_deleted = 0 AND is_folder = 0 ORDER BY create_time DESC")
    List<PublicFile> selectByCategory(@Param("category") String category);

    /**
     * 搜索公共文件（不包括已删除）
     */
    @Select("SELECT * FROM public_file WHERE file_name LIKE CONCAT('%', #{keyword}, '%') AND is_deleted = 0 ORDER BY create_time DESC")
    List<PublicFile> searchByKeyword(@Param("keyword") String keyword);

    // ==================== 插入操作 ====================
    
    /**
     * 插入公共文件
     */
    @Insert("INSERT INTO public_file (parent_id, file_name, is_folder, storage_id, file_size, file_type, category, download_count, is_deleted) " +
            "VALUES (#{parentId}, #{fileName}, #{isFolder}, #{storageId}, #{fileSize}, #{fileType}, #{category}, #{downloadCount}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "fileId")
    int insert(PublicFile publicFile);

    // ==================== 更新操作 ====================
    
    /**
     * 重命名文件
     */
    @Update("UPDATE public_file SET file_name = #{newName}, update_time = NOW() WHERE file_id = #{fileId}")
    int updateFileName(@Param("fileId") Long fileId, @Param("newName") String newName);

    /**
     * 移动文件（修改父ID）
     */
    @Update("UPDATE public_file SET parent_id = #{newParentId}, update_time = NOW() WHERE file_id = #{fileId}")
    int updateParentId(@Param("fileId") Long fileId, @Param("newParentId") Long newParentId);

    /**
     * 增加下载次数
     */
    @Update("UPDATE public_file SET download_count = download_count + 1 WHERE file_id = #{fileId}")
    int incrementDownloadCount(@Param("fileId") Long fileId);

    // ==================== 删除操作 ====================
    
    /**
     * 软删除（移入回收站）
     */
    @Update("UPDATE public_file SET is_deleted = 1, update_time = NOW() WHERE file_id = #{fileId}")
    int softDelete(@Param("fileId") Long fileId);

    /**
     * 恢复文件
     */
    @Update("UPDATE public_file SET is_deleted = 0, update_time = NOW() WHERE file_id = #{fileId}")
    int restore(@Param("fileId") Long fileId);

    /**
     * 彻底删除文件
     */
    @Delete("DELETE FROM public_file WHERE file_id = #{fileId}")
    int permanentDelete(@Param("fileId") Long fileId);

    // ==================== 回收站 ====================
    
    /**
     * 获取回收站文件列表
     */
    @Select("SELECT * FROM public_file WHERE is_deleted = 1 ORDER BY update_time DESC")
    List<PublicFile> selectDeleted();

    /**
     * 清空回收站
     */
    @Delete("DELETE FROM public_file WHERE is_deleted = 1")
    int emptyRecycleBin();

    // ==================== 统计查询 ====================
    
    /**
     * 统计文件总数（不包括已删除）
     */
    @Select("SELECT COUNT(*) FROM public_file WHERE is_deleted = 0 AND is_folder = 0")
    long countFiles();

    /**
     * 统计文件总大小（不包括已删除）
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM public_file WHERE is_deleted = 0 AND is_folder = 0")
    long sumFileSize();

    /**
     * 统计总下载次数
     */
    @Select("SELECT COALESCE(SUM(download_count), 0) FROM public_file WHERE is_deleted = 0 AND is_folder = 0")
    long sumDownloadCount();
    
    /**
     * 统计指定文件夹下的项目数量（不包括已删除）
     */
    @Select("SELECT COUNT(*) FROM public_file WHERE parent_id = #{parentId} AND is_deleted = 0")
    int countByParentId(@Param("parentId") Long parentId);
}
