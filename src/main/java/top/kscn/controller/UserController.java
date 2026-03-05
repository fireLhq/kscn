package top.kscn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.common.Result;
import top.kscn.entity.User;
import top.kscn.entity.dto.*;
import top.kscn.service.MailService;
import top.kscn.service.RedisCacheService;
import top.kscn.service.UserService;
import top.kscn.utils.SecurityInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

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

    @Value("${file.storage-root}")
    private String storageRoot;

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
    public Result<?> login(@RequestBody @Valid LoginDTO dto, HttpServletResponse response) {
        String token = userService.login(dto);
        if (token == null) {
            return Result.error("用户名或密码错误");
        }
        
        // 将token设置到HttpOnly Cookie中
        Cookie cookie = new Cookie("jwt_token", token);
        cookie.setHttpOnly(true); // 防止XSS攻击
        cookie.setSecure(false); // 生产环境应设置为true（需要HTTPS）
        cookie.setPath("/"); // Cookie的作用路径
        cookie.setMaxAge(10 * 60 * 60); // 10小时，与JWT过期时间一致
        response.addCookie(cookie);
        
        return Result.success("登录成功", token);
    }
    
    @PostMapping("/auth/logout")
    public Result<?> logout(HttpServletResponse response) {
        // 清除Cookie
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 立即过期
        response.addCookie(cookie);
        
        return Result.success("登出成功");
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

    // ==================== 头像管理 ====================

    /**
     * 上传头像
     * 统一处理为 256x256 的 PNG 图片
     * 物理覆盖旧头像
     */
    @PostMapping("/regular/uploadAvatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = SecurityInfo.getUserId();
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }
            
            // 验证文件大小（限制10MB，因为前端裁剪后会比较大）
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("头像文件大小不能超过10MB");
            }
            
            // 创建头像目录
            Path avatarDir = Paths.get(storageRoot, "avatar");
            Files.createDirectories(avatarDir);
            
            // 获取用户当前头像信息
            User user = userService.getUser(userId);
            String oldAvatar = user.getAvatar();
            
            // 生成新的文件名（使用用户ID，这样每次上传都会覆盖同一个文件）
            String newFileName = userId + ".png";
            Path avatarPath = avatarDir.resolve(newFileName);
            
            // 读取上传的图片
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                return Result.error("无法读取图片文件，请确保上传的是有效的图片");
            }
            
            // 使用 Thumbnailator 处理图片：
            // 1. 调整大小为 256x256
            // 2. 转换为 PNG 格式
            // 3. 保持图片质量
            Thumbnails.of(originalImage)
                    .size(256, 256)
                    .outputFormat("png")
                    .toFile(avatarPath.toFile());
            
            // 如果旧头像存在且不是默认头像，并且文件名不同（虽然现在都是userId.png，但保留这个逻辑以防万一）
            if (oldAvatar != null && !oldAvatar.equals("default_avatar.png") && !oldAvatar.equals(newFileName)) {
                try {
                    Path oldAvatarPath = avatarDir.resolve(oldAvatar);
                    Files.deleteIfExists(oldAvatarPath);
                } catch (Exception e) {
                    // 忽略删除旧头像的错误
                    System.err.println("删除旧头像失败: " + e.getMessage());
                }
            }
            
            // 更新数据库
            User updateUser = new User();
            updateUser.setUserId(userId);
            updateUser.setAvatar(newFileName);
            updateUser.setUpdateTime(new Date());
            userService.updateUser(updateUser);
            
            return Result.success("头像上传成功", newFileName);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取头像
     * 支持未登录访问，用于显示其他用户的头像
     */
    @GetMapping("/auth/avatar/{filename}")
    public void getAvatar(@PathVariable String filename, HttpServletResponse response) {
        try {
            // 构建头像文件路径
            Path avatarPath = Paths.get(storageRoot, "avatar", filename);
            File avatarFile = avatarPath.toFile();
            
            // 如果文件不存在，尝试返回默认头像
            if (!avatarFile.exists()) {
                // 如果请求的就是默认头像，或者文件不存在，返回默认头像
                Path defaultAvatarPath = Paths.get(storageRoot, "avatar", "default_avatar.png");
                File defaultAvatarFile = defaultAvatarPath.toFile();
                
                if (defaultAvatarFile.exists()) {
                    avatarFile = defaultAvatarFile;
                    avatarPath = defaultAvatarPath;
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "头像不存在");
                    return;
                }
            }
            
            // 设置响应头
            String contentType = Files.probeContentType(avatarPath);
            if (contentType == null) {
                contentType = "image/png";
            }
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "max-age=86400"); // 缓存1天
            response.setContentLengthLong(avatarFile.length());
            
            // 输出文件
            try (InputStream in = new BufferedInputStream(new FileInputStream(avatarFile));
                 OutputStream out = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取头像失败");
            } catch (IOException ex) {
                // 忽略
            }
        }
    }

}
