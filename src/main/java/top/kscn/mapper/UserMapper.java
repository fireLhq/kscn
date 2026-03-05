package top.kscn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.kscn.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    @Select("select * from user where email = #{email}")
    User selectByEmail(String email);

    /**
     * 更新用户已使用空间
     * @param userId 用户ID
     * @param sizeChange 空间变化量（正数增加，负数减少）
     */
    @Update("UPDATE user SET used_space = used_space + #{sizeChange} WHERE user_id = #{userId}")
    int updateUsedSpace(@Param("userId") Long userId, @Param("sizeChange") Long sizeChange);
}
