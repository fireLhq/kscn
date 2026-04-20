package top.kscn.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import top.kscn.entity.User;
import top.kscn.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {

        // 账号可为用户名或邮箱，直接按传入内容查询
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .and(wrapper -> wrapper
                                .eq(User::getUsername, account)
                                .or()
                                .eq(User::getEmail, account)
                        )
                        .eq(User::getIsDeleted, 0)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        return new SecurityUser(user);
    }
}