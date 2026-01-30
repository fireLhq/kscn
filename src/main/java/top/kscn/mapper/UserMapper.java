package top.kscn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.kscn.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    @Select("select * from user where email = #{email}")
    User selectByEmail(String email);
}
