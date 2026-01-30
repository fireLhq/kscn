package top.kscn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import top.kscn.common.Result;
import top.kscn.entity.User;
import top.kscn.entity.dto.*;
import top.kscn.service.MailService;
import top.kscn.service.RedisCacheService;
import top.kscn.service.UserService;
import top.kscn.utils.SecurityInfo;

import java.util.Date;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/auth/registerRequest")
    public Result<?> registerRequest(@RequestBody @Valid RegisterDTO dto) {
        userService.registerRequest(dto);
        return Result.success("验证码已发送，请检查您的邮箱");
    }

    @PostMapping("/auth/registerVerify")
    public Result<?> registerVerify(@RequestBody @Valid VerifyDTO dto) {
        userService.registerVerify(dto);
        return Result.success("注册成功");
    }

    @PostMapping("/auth/login")
    public Result<?> login(@RequestBody @Valid LoginDTO dto) {
        String token = userService.login(dto);
        if (token == null) {
            return Result.error("用户名或密码错误");
        }
        return Result.success("登录成功", token);
    }

    @PostMapping("/auth/sendEmailCode")
    public Result<?> authSendEmailCode(@RequestBody @Valid EmailDTO dto) {
        // 验证用户是否存在
        User user = userService.getUserByEmail(dto.getEmail());
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 验证用户是否已激活
        if (user.getStatus() != 1) {
            return Result.error("用户未激活");
        }
        // 生成随机6位数字验证码
        String code = RandomStringUtils.randomNumeric(6);
        redisCacheService.saveEmailCode(dto.getEmail(), code);
        String subject = "密码重置验证码";
        String content = "您的密码重置验证码为：" + code + "，该验证码5分钟内有效，请勿泄露给他人。";
        mailService.sendMessage(dto.getEmail(), subject, content);
        return Result.success("验证码已发送，请检查您的邮箱");
    }

    @PutMapping("/auth/updatePassword")
    public Result<?> authUpdatePassword(@RequestBody @Valid ResetPasswordDTO dto) {
        // 验证验证码
        String code = redisCacheService.getEmailCode(dto.getEmail());
        if (code == null || !code.equals(dto.getCode())) {
            return Result.error("验证码错误或已过期");
        }
        // 验证用户是否存在
        User user = userService.getUserByEmail(dto.getEmail());
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(new Date());
        int update = userService.updateUser(user);
        if (update > 0) {
            // 删除验证码
            redisCacheService.removeEmailCode(dto.getEmail());
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @GetMapping("/regular/getUser")
    public Result<?> getUser() {
        Long userId = SecurityInfo.getUserId();
        User user = userService.getUser(userId);
        return Result.success(null, user);
    }

    @PutMapping("/regular/updateUser")
    public Result<?> updateUser(@RequestBody User user) {
        Long userId = SecurityInfo.getUserId();
        user.setUserId(userId);
        user.setUsername(null);
        user.setEmail(null);
        user.setPassword(null);
        user.setRole(null);
        user.setAvatar(null);
        user.setStatus(null);
        user.setCreateTime(null);
        user.setUpdateTime(new Date());
        int update = userService.updateUser(user);
        if (update > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @PutMapping("/regular/updateUsername")
    public Result<?> updateUsername(@RequestBody @Valid UsernameDTO dto) {
        // 判断用户名是否合法（必须由字母、下划线、数字，不能以数字开头）
        if (!dto.getUsername().matches("^[a-zA-Z_][a-zA-Z0-9_]{3,15}$")) {
            return Result.error("用户名不合法，必须由字母、下划线、数字组成，且长度在4-16之间，不能以数字开头");
        }
        // 判断用户名是否已存在
        if (userService.getUserByUsername(dto.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        Long userId = SecurityInfo.getUserId();
        User user = new User();
        user.setUserId(userId);
        user.setUsername(dto.getUsername());
        user.setUpdateTime(new Date());
        int update = userService.updateUser(user);
        if (update > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @PostMapping("/regular/sendEmailCode")
    public Result<?> sendEmailCode(@RequestBody @Valid EmailDTO dto) {
        // 生成随机6位数字验证码
        String code = RandomStringUtils.randomNumeric(6);
        redisCacheService.saveEmailCode(dto.getEmail(), code);
        String subject = "邮箱修改验证码";
        String content = "您的邮箱修改验证码为：" + code + "，该验证码5分钟内有效，请勿泄露给他人。";
        mailService.sendMessage(dto.getEmail(), subject, content);
        return Result.success("验证码已发送，请检查您的邮箱");
    }

    @PutMapping("/regular/updateEmail")
    public Result<?> updateEmail(@RequestBody @Valid VerifyDTO dto) {
        // 验证验证码
        String code = redisCacheService.getEmailCode(dto.getEmail());
        if (code == null || !code.equals(dto.getCode())) {
            return Result.error("验证码错误或已过期");
        }
        // 判断邮箱是否已存在
        if (userService.getUserByEmail(dto.getEmail()) != null) {
            return Result.error("邮箱已存在");
        }
        Long userId = SecurityInfo.getUserId();
        User user = new User();
        user.setUserId(userId);
        user.setEmail(dto.getEmail());
        user.setUpdateTime(new Date());
        int update = userService.updateUser(user);
        if (update > 0) {
            // 删除验证码
            redisCacheService.removeEmailCode(dto.getEmail());
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @PutMapping("/regular/updatePassword")
    public Result<?> updatePassword(@RequestBody @Valid PasswordDTO dto) {
        Long userId = SecurityInfo.getUserId();
        // 判断旧密码是否正确
        User user = userService.getUser(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(new Date());
        int update = userService.updateUser(user);
        if (update > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @GetMapping("/admin/userList")
    public Result<?> adminUserList(@Param("dto") UserDTO dto) {
        Page<User> page = userService.userList(dto);
        return Result.success(null, page);
    }

    @PostMapping("/admin/addUser")
    public Result<?> adminAddUser(@RequestBody @Valid User user) {
        // 判断用户名是否合法（必须由字母、下划线、数字，不能以数字开头）
        if (!user.getUsername().matches("^[a-zA-Z_][a-zA-Z0-9_]{3,15}$")) {
            return Result.error("用户名不合法，必须由字母、下划线、数字组成，且长度在4-16之间，不能以数字开头");
        }
        // 判断用户名是否已存在
        if (userService.getUserByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        // 判断邮箱是否已存在
        if (userService.getUserByEmail(user.getEmail()) != null) {
            return Result.error("邮箱已存在");
        }
        // 设置默认密码为123456
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAvatar("default_avatar.png");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        int save = userService.addUser(user);
        if (save > 0) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    @PutMapping("/admin/updateUser")
    public Result<?> adminUpdateUser(@RequestBody @Valid User user) {
        // 管理员不能修改自己的角色和状态
        Long userId = SecurityInfo.getUserId();
        User currentUser = userService.getUser(userId);
        if (user.getUserId().equals(userId)) {
            user.setRole(currentUser.getRole());
            user.setStatus(currentUser.getStatus());
        }
        // 判断用户名是否合法（必须由字母、下划线、数字，不能以数字开头）
        if (user.getUsername() != null && !user.getUsername().matches("^[a-zA-Z_][a-zA-Z0-9_]{3,15}$")) {
            return Result.error("用户名不合法，必须由字母、下划线、数字组成，且长度在4-16之间，不能以数字开头");
        }
        // 判断用户名是否已存在
        if (user.getUsername() != null) {
            User oldUser = userService.getUserByUsername(user.getUsername());
            if (oldUser != null && !oldUser.getUserId().equals(user.getUserId())) {
                return Result.error("用户名已存在");
            }
        }
        // 判断邮箱是否已存在
        if (user.getEmail() != null) {
            User oldUser = userService.getUserByEmail(user.getEmail());
            if (oldUser != null && !oldUser.getUserId().equals(user.getUserId())) {
                return Result.error("邮箱已存在");
            }
        }
        user.setPassword(null);
        user.setUpdateTime(new Date());
        int update = userService.updateUser(user);
        if (update > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/admin/deleteUser/{userId}")
    public Result<?> adminDeleteUser(@PathVariable Long userId) {
        int delete = userService.deleteUser(userId);
        if (delete > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

}
