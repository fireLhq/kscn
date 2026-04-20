package top.kscn.service.impl;

import com.aliyun.captcha20230305.Client;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.entity.Captcha;
import top.kscn.entity.User;
import top.kscn.entity.dto.auth.AuthLoginDTO;
import top.kscn.entity.dto.auth.AuthPasswordCodeDTO;
import top.kscn.entity.dto.auth.AuthPasswordDTO;
import top.kscn.entity.dto.auth.AuthRegisterCodeDTO;
import top.kscn.entity.dto.auth.AuthRegisterDTO;
import top.kscn.entity.dto.user.UserDTO;
import top.kscn.entity.dto.user.UserProfileDTO;
import top.kscn.entity.dto.user.UserProfileEmailCodeDTO;
import top.kscn.entity.dto.user.UserProfileEmailDTO;
import top.kscn.entity.dto.user.UserProfilePasswordDTO;
import top.kscn.entity.dto.user.UserProfileUsernameDTO;
import top.kscn.exception.CustomException;
import top.kscn.mapper.UserMapper;
import top.kscn.security.SecurityUser;
import top.kscn.security.UserDetailsServiceImpl;
import top.kscn.service.MailService;
import top.kscn.service.RedisCacheService;
import top.kscn.service.UserFileService;
import top.kscn.service.UserService;
import top.kscn.utils.JwtUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private Captcha captcha;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserFileService userFileService;

    @Value("${file.storage-root}")
    private String storageRoot;

    @Override
    public Boolean postAuthCaptchaVerify(String captchaVerifyParam) throws Exception {
        Config config = new Config();
        config.accessKeyId = captcha.getAccessKeyId();
        config.accessKeySecret = captcha.getAccessKeySecret();
        config.endpoint = "captcha.cn-shanghai.aliyuncs.com";
        config.connectTimeout = 5000;
        config.readTimeout = 5000;

        Client client = new Client(config);
        VerifyIntelligentCaptchaRequest request = new VerifyIntelligentCaptchaRequest();
        request.captchaVerifyParam = captchaVerifyParam;

        try {
            VerifyIntelligentCaptchaResponse resp = client.verifyIntelligentCaptcha(request);
            return resp.body.result.verifyResult;
        } catch (TeaException te) {
            log.error("[验证码] 验证异常", te);
            return true;
        } catch (Exception e) {
            log.error("[验证码] 验证异常", new TeaException(e.getMessage(), e));
            return true;
        }
    }

    @Override
    @Transactional
    public void postAuthRegisterCode(AuthRegisterCodeDTO dto) {
        if (existsByEmail(dto.getEmail())) throw new CustomException(400, "该邮箱已注册");
        String code = RandomStringUtils.randomNumeric(6);
        redisCacheService.saveRegisterInfo(dto.getEmail(), dto.getPassword(), code);
        mailService.sendMessage(dto.getEmail(), "注册验证码", "您的注册验证码是：" + code + "，5分钟内有效");
    }

    @Override
    @Transactional
    public void postAuthRegister(AuthRegisterDTO dto) {
        Map<String, String> tempUser = redisCacheService.getTempUser(dto.getEmail());
        if (tempUser == null) throw new CustomException(400, "注册会话已过期");
        if (!dto.getCode().equals(tempUser.get("code"))) throw new CustomException(400, "验证码错误或已过期");
        if (existsByEmail(dto.getEmail())) throw new CustomException(400, "该邮箱已注册");

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(tempUser.get("password")));
        if (!save(user)) throw new CustomException(500, "注册失败");
        redisCacheService.removeTempUser(dto.getEmail());
    }

    @Override
    public String postAuthLogin(AuthLoginDTO dto) {
        SecurityUser user = (SecurityUser) userDetailsService.loadUserByUsername(dto.getAccount());
        if (!user.isEnabled()) {
            throw new CustomException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(401, "账号或密码错误");
        }
        return JwtUtil.createToken(user.getUserId(), user.getRole());
    }

    @Override
    public void postAuthPasswordCode(AuthPasswordCodeDTO dto) {
        User user = getByEmail(dto.getEmail());
        if (user == null) throw new CustomException(404, "用户不存在");
        if (user.getStatus() != 1) throw new CustomException(403, "账号已被禁用");
        String code = RandomStringUtils.randomNumeric(6);
        redisCacheService.saveEmailCode(dto.getEmail(), code);
        mailService.sendMessage(dto.getEmail(), "密码重置验证码", "您的密码重置验证码为：" + code + "，请在5分钟内完成验证，请勿泄露给他人。");
    }

    @Override
    public void putAuthPassword(AuthPasswordDTO dto) {
        String code = redisCacheService.getEmailCode(dto.getEmail());
        if (code == null || !code.equals(dto.getCode())) throw new CustomException(400, "验证码错误或已过期");
        User user = getByEmail(dto.getEmail());
        if (user == null) throw new CustomException(400, "重置请求无效，请检查邮箱和验证码");
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        if (!updateById(user)) throw new CustomException(500, "密码重置失败");
        redisCacheService.removeEmailCode(dto.getEmail());
    }

    @Override
    public User getUserProfile(Long userId) {
        return getById(userId);
    }

    @Override
    public void putUserProfile(Long userId, UserProfileDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setUserId(userId);
        if (!updateById(user)) throw new CustomException(500, "更新失败");
    }

    @Override
    public void putUserProfileUsername(Long userId, UserProfileUsernameDTO dto) {
        User oldUser = getByUsername(dto.getUsername());
        if (oldUser != null && !oldUser.getUserId().equals(userId)) {
            throw new CustomException(400, "用户名已存在");
        }
        User user = new User();
        user.setUserId(userId);
        user.setUsername(dto.getUsername());
        if (!updateById(user)) throw new CustomException(500, "更新失败");
    }

    @Override
    public void postUserProfileEmailCode(UserProfileEmailCodeDTO dto) {
        if (existsByEmail(dto.getEmail())) throw new CustomException(400, "邮箱已存在");
        String code = RandomStringUtils.randomNumeric(6);
        redisCacheService.saveEmailCode(dto.getEmail(), code);
        mailService.sendMessage(dto.getEmail(), "邮箱修改验证码", "您的邮箱修改验证码为：" + code + "，请在5分钟内完成验证，请勿泄露给他人。");
    }

    @Override
    public void putUserProfileEmail(Long userId, UserProfileEmailDTO dto) {
        String code = redisCacheService.getEmailCode(dto.getEmail());
        if (code == null || !code.equals(dto.getCode())) throw new CustomException(400, "验证码错误或已过期");

        User oldUser = getByEmail(dto.getEmail());
        if (oldUser != null && !oldUser.getUserId().equals(userId)) {
            throw new CustomException(400, "邮箱已存在");
        }

        User user = new User();
        user.setUserId(userId);
        user.setEmail(dto.getEmail());
        if (!updateById(user)) throw new CustomException(500, "更新失败");
        redisCacheService.removeEmailCode(dto.getEmail());
    }

    @Override
    public void putUserProfilePassword(Long userId, UserProfilePasswordDTO dto) {
        User user = getById(userId);
        if (user == null) throw new CustomException(404, "用户不存在");
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new CustomException(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        if (!updateById(user)) throw new CustomException(500, "更新失败");
    }

    @Override
    public String postUserProfileAvatar(Long userId, MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(400, "只能上传图片文件");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new CustomException(400, "头像文件大小不能超过10MB");
        }

        Path avatarDir = Paths.get(storageRoot, "avatar");
        Files.createDirectories(avatarDir);

        User user = getUserProfile(userId);
        if (user == null) {
            throw new CustomException(404, "用户不存在");
        }

        String oldAvatar = user.getAvatar();
        String newFileName = userId + ".png";
        Path avatarPath = avatarDir.resolve(newFileName);

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new CustomException(400, "无法读取图片文件，请确保上传的是有效的图片");
        }

        Thumbnails.of(originalImage).size(256, 256).outputFormat("png").toFile(avatarPath.toFile());

        if (oldAvatar != null && !oldAvatar.equals("default_avatar.png") && !oldAvatar.equals(newFileName)) {
            try {
                Files.deleteIfExists(avatarDir.resolve(oldAvatar));
            } catch (Exception ignored) {
            }
        }

        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setAvatar(newFileName);
        if (!updateById(updateUser)) {
            throw new CustomException(500, "头像上传失败");
        }
        return newFileName;
    }

    @Override
    public void getUserAvatar(String filename, HttpServletResponse response) {
        try {
            Path avatarPath = Paths.get(storageRoot, "avatar", filename);
            File avatarFile = avatarPath.toFile();
            if (!avatarFile.exists()) {
                Path defaultPath = Paths.get(storageRoot, "avatar", "default_avatar.png");
                File defaultFile = defaultPath.toFile();
                if (defaultFile.exists()) {
                    avatarFile = defaultFile;
                    avatarPath = defaultPath;
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "头像不存在");
                    return;
                }
            }
            String contentType = Files.probeContentType(avatarPath);
            response.setContentType(contentType != null ? contentType : "image/png");
            response.setHeader("Cache-Control", "max-age=86400");
            response.setContentLengthLong(avatarFile.length());
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
            throw new CustomException(500, "获取头像失败");
        }
    }

    @Override
    public Page<User> getAdminUserPage(UserDTO dto, Long page, Long size) {
        long pageNo = (page == null || page < 1) ? 1L : page;
        long pageSize = (size == null || size < 1) ? 10L : size;
        Page<User> pageData = new Page<>(pageNo, pageSize);
        return page(pageData, Wrappers.<User>lambdaQuery()
                .eq(User::getIsDeleted, 0)
                .eq(dto.getUserId() != null, User::getUserId, dto.getUserId())
                .like(StringUtils.isNotBlank(dto.getUsername()), User::getUsername, dto.getUsername())
                .like(StringUtils.isNotBlank(dto.getEmail()), User::getEmail, dto.getEmail())
                .like(StringUtils.isNotBlank(dto.getNickname()), User::getNickname, dto.getNickname())
                .eq(dto.getRole() != null, User::getRole, dto.getRole())
                .eq(dto.getSex() != null, User::getSex, dto.getSex())
                .eq(dto.getStatus() != null, User::getStatus, dto.getStatus())
                .orderByDesc(User::getCreateTime));
    }

    @Override
    public Map<String, Object> getAdminUserStats() {
        Long totalUsers = lambdaQuery().eq(User::getIsDeleted, 0).count();
        Long adminUsers = lambdaQuery().eq(User::getIsDeleted, 0).eq(User::getRole, 0).count();
        Long normalUsers = lambdaQuery().eq(User::getIsDeleted, 0).eq(User::getRole, 1).count();
        Long activeUsers = lambdaQuery().eq(User::getIsDeleted, 0).eq(User::getStatus, 1).count();
        Long disabledUsers = lambdaQuery().eq(User::getIsDeleted, 0).eq(User::getStatus, 0).count();
        Long totalSpace = lambdaQuery().eq(User::getIsDeleted, 0).list().stream()
                .map(User::getTotalSpace)
                .filter(java.util.Objects::nonNull)
                .reduce(0L, Long::sum);
        Long usedSpace = lambdaQuery().eq(User::getIsDeleted, 0).list().stream()
                .map(User::getUsedSpace)
                .filter(java.util.Objects::nonNull)
                .reduce(0L, Long::sum);

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", totalUsers == null ? 0L : totalUsers);
        stats.put("adminUsers", adminUsers == null ? 0L : adminUsers);
        stats.put("normalUsers", normalUsers == null ? 0L : normalUsers);
        stats.put("activeUsers", activeUsers == null ? 0L : activeUsers);
        stats.put("disabledUsers", disabledUsers == null ? 0L : disabledUsers);
        stats.put("totalSpace", totalSpace == null ? 0L : totalSpace);
        stats.put("usedSpace", usedSpace == null ? 0L : usedSpace);
        return stats;
    }

    @Override
    public long countRegisteredUsers() {
        Long totalUsers = lambdaQuery().eq(User::getIsDeleted, 0).count();
        return totalUsers == null ? 0L : totalUsers;
    }

    @Override
    public void postAdminUser(UserDTO dto) {
        if (StringUtils.isNotBlank(dto.getUsername()) && existsByUsername(dto.getUsername())) {
            throw new CustomException(400, "用户名已存在");
        }
        if (existsByEmail(dto.getEmail())) {
            throw new CustomException(400, "邮箱已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (!save(user)) throw new CustomException(500, "添加失败");
    }

    @Override
    public void putAdminUser(Long userId, UserDTO dto, Long currentUserId) {
        User currentUser = getById(currentUserId);
        if (currentUser == null) throw new CustomException(404, "当前用户不存在");

        if (StringUtils.isNotBlank(dto.getUsername())) {
            User oldUser = getByUsername(dto.getUsername());
            if (oldUser != null && !oldUser.getUserId().equals(userId)) {
                throw new CustomException(400, "用户名已存在");
            }
        }
        if (StringUtils.isNotBlank(dto.getEmail())) {
            User oldUser = getByEmail(dto.getEmail());
            if (oldUser != null && !oldUser.getUserId().equals(userId)) {
                throw new CustomException(400, "邮箱已存在");
            }
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user, "password", "userId", "isDeleted", "deleteTime", "createTime", "updateTime", "totalSpace", "usedSpace", "avatar");
        user.setUserId(userId);
        user.setPassword(null);
        if (userId.equals(currentUserId)) {
            user.setRole(currentUser.getRole());
            user.setStatus(currentUser.getStatus());
        }
        if (!updateById(user)) throw new CustomException(500, "更新失败");
    }

    @Override
    public void putAdminUserDefaultAvatar(Long userId) {
        User user = getById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new CustomException(404, "用户不存在");
        }

        String oldAvatar = user.getAvatar();
        if (StringUtils.isNotBlank(oldAvatar) && !"default_avatar.png".equals(oldAvatar)) {
            try {
                Files.deleteIfExists(Paths.get(storageRoot, "avatar", oldAvatar));
            } catch (Exception ignored) {
            }
        }

        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setAvatar("default_avatar.png");
        if (!updateById(updateUser)) throw new CustomException(500, "设置默认头像失败");
    }

    @Override
    public void putAdminUserRecalculateUsedSpace(Long userId) {
        User user = getById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new CustomException(404, "用户不存在");
        }

        userFileService.recalculateUsedSpace(userId);
    }

    @Override
    public void deleteAdminUser(Long userId) {
        long ts = System.currentTimeMillis();
        String deletedUsername = "0d_" + userId + "_" + ts;
        String deletedEmail = "deleted+" + userId + "." + ts + "@deleted.local";

        User deletedUser = new User();
        deletedUser.setUserId(userId);
        deletedUser.setUsername(deletedUsername);
        deletedUser.setEmail(deletedEmail);
        deletedUser.setIsDeleted(1);
        deletedUser.setDeleteTime(new Date());
        deletedUser.setStatus(0);
        if (!updateById(deletedUser)) throw new CustomException(500, "删除失败");
    }

    private User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0)
                .last("limit 1"));
    }

    private User getByEmail(String email) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getIsDeleted, 0)
                .last("limit 1"));
    }

    private boolean existsByUsername(String username) {
        return getByUsername(username) != null;
    }

    private boolean existsByEmail(String email) {
        return getByEmail(email) != null;
    }
}
