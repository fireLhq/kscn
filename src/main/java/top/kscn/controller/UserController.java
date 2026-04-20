package top.kscn.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.kscn.common.Result;
import top.kscn.entity.User;
import top.kscn.entity.dto.user.UserProfileDTO;
import top.kscn.entity.dto.user.UserProfileEmailCodeDTO;
import top.kscn.entity.dto.user.UserProfileEmailDTO;
import top.kscn.entity.dto.user.UserProfilePasswordDTO;
import top.kscn.entity.dto.user.UserProfileUsernameDTO;
import top.kscn.exception.CustomException;
import top.kscn.service.UserService;
import top.kscn.utils.SecurityContextUtil;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public Result<?> getUserProfile() {
        Long userId = SecurityContextUtil.currentUserId();
        User user = userService.getUserProfile(userId);
        if (user == null) throw new CustomException(404, "用户不存在");
        user.setPassword(null);
        return Result.success(null, user);
    }

    @PutMapping("/profile")
    public Result<?> putUserProfile(@RequestBody @Valid UserProfileDTO dto) {
        userService.putUserProfile(SecurityContextUtil.currentUserId(), dto);
        return Result.success("更新成功", null);
    }

    @PutMapping("/profile/username")
    public Result<?> putUserProfileUsername(@RequestBody @Valid UserProfileUsernameDTO dto) {
        userService.putUserProfileUsername(SecurityContextUtil.currentUserId(), dto);
        return Result.success("更新成功", null);
    }

    @PostMapping("/profile/email/code")
    public Result<?> postUserProfileEmailCode(@RequestBody @Valid UserProfileEmailCodeDTO dto) {
        userService.postUserProfileEmailCode(dto);
        return Result.success("验证码已发送，请查收您的邮箱", null);
    }

    @PutMapping("/profile/email")
    public Result<?> putUserProfileEmail(@RequestBody @Valid UserProfileEmailDTO dto) {
        userService.putUserProfileEmail(SecurityContextUtil.currentUserId(), dto);
        return Result.success("更新成功", null);
    }

    @PutMapping("/profile/password")
    public Result<?> putUserProfilePassword(@RequestBody @Valid UserProfilePasswordDTO dto) {
        userService.putUserProfilePassword(SecurityContextUtil.currentUserId(), dto);
        return Result.success("更新成功", null);
    }

    @PostMapping("/profile/avatar")
    public Result<?> postUserProfileAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        String newFileName = userService.postUserProfileAvatar(SecurityContextUtil.currentUserId(), file);
        return Result.success("头像上传成功", newFileName);
    }

    @GetMapping("/avatar/{filename}")
    public void getUserAvatar(@PathVariable String filename, HttpServletResponse response) {
        userService.getUserAvatar(filename, response);
    }

    @GetMapping("/count")
    @PreAuthorize("permitAll()")
    public Result<?> countRegisteredUsers() {
        return Result.success(null, userService.countRegisteredUsers());
    }
}
