package top.kscn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.kscn.entity.User;
import top.kscn.entity.dto.LoginDTO;
import top.kscn.entity.dto.RegisterDTO;
import top.kscn.entity.dto.UserDTO;
import top.kscn.entity.dto.VerifyDTO;
import top.kscn.exception.CustomException;
import top.kscn.mapper.UserMapper;
import top.kscn.service.MailService;
import top.kscn.service.RedisCacheService;
import top.kscn.service.UserService;
import top.kscn.utils.JwtUtil;

import java.util.Date;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerRequest(RegisterDTO dto) {

        // 1. 验证邮箱唯一性
        if (userMapper.selectByEmail(dto.getEmail()) != null) {
            throw new CustomException(400, "该邮箱已注册");
        }

        // 2. 生成验证码（6位随机数字）
        String code = RandomStringUtils.randomNumeric(6);

        // 3. 保存临时数据到Redis（有效期5分钟）
        redisCacheService.saveRegisterInfo(dto.getEmail(), dto.getPassword(), code);

        // 4. 发送邮件（需要实现MailService）
        String subject = "注册验证码";
        String content = "您的注册验证码是：" + code + "，5分钟内有效";
        mailService.sendMessage(dto.getEmail(), subject, content);
    }

    @Override
    @Transactional
    public void registerVerify(VerifyDTO dto) {
        // 1. 获取缓存数据
        Map<String, String> tempUser = redisCacheService.getTempUser(dto.getEmail());
        if (tempUser == null) {
            throw new CustomException(400, "注册会话已过期");
        }

        // 2. 验证验证码
        if (!dto.getCode().equals(tempUser.get("code"))) {
            throw new CustomException(400, "验证码错误或已过期");
        }

        // 3. 创建用户实体
        User user = new User();
        user.setEmail(dto.getEmail()); // 邮箱
        user.setPassword(passwordEncoder.encode(tempUser.get("password"))); // 加密密码
        user.setRole(1); // 默认角色为普通用户
        user.setAvatar("default_avatar.png"); // 默认头像
        user.setSex(2); // 默认性别为不愿透露
        user.setStatus(1); // 默认状态为启用
        user.setCreateTime(new Date()); // 创建时间
        user.setUpdateTime(new Date()); // 更新时间

        // 4. 保存到数据库
        userMapper.insert(user);

        // 5. 清除缓存
        redisCacheService.removeTempUser(dto.getEmail());
    }

    @Override
    public String login(LoginDTO dto) {
        // 根据邮箱查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (dto.getAccount().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            queryWrapper.eq("email", dto.getAccount());
        } else {
            queryWrapper.eq("username", dto.getAccount());
        }
        User user = userMapper.selectOne(queryWrapper);

        // 用户不存在或密码不匹配
        if (user == null) {
            throw new CustomException(400, "用户不存在");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(400, "密码错误");
        }

        if (user.getStatus() == 0) {
            throw new CustomException(400, "用户已禁用");
        }

        // 登录成功，生成 JWT Token
        return JwtUtil.createToken(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    @Override
    public User getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public int updateUser(User user) {
        return userMapper.updateById(user);
    }

    @Override
    public Page<User> userList(UserDTO dto) {
        Page<User> page = new Page<>(dto.getPage(), dto.getSize());
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (dto.getUsername() != null) {
            wrapper.like("username", dto.getUsername());
        }
        if (dto.getEmail() != null) {
            wrapper.like("email", dto.getEmail());
        }
        if (dto.getNickname() != null) {
            wrapper.like("nickname", dto.getNickname());
        }
        if (dto.getRole() != null) {
            wrapper.eq("role", dto.getRole());
        }
        if (dto.getSex() != null) {
            wrapper.eq("sex", dto.getSex());
        }
        if (dto.getStatus() != null) {
            wrapper.eq("status", dto.getStatus());
        }
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public int addUser(User user) {
        return userMapper.insert(user);
    }

    @Override
    public int deleteUser(Long userId) {
        return userMapper.deleteById(userId);
    }
}
