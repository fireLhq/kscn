package top.kscn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.kscn.common.Result;
import top.kscn.annotation.OperationLog;
import top.kscn.entity.User;
import top.kscn.entity.dto.user.UserDTO;
import top.kscn.service.UserService;
import top.kscn.utils.SecurityContextUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<?> getAdminUserPage(UserDTO dto,
                                      @RequestParam(required = false) Long page,
                                      @RequestParam(required = false) Long size) {
        Page<User> pageData = userService.getAdminUserPage(dto, page, size);
        return Result.success(null, pageData);
    }

    @GetMapping("/stats")
    public Result<?> getAdminUserStats() {
        Map<String, Object> stats = userService.getAdminUserStats();
        return Result.success(null, stats);
    }

    @PostMapping
    @OperationLog(eventType = "ADMIN", action = "CREATE_USER", resourceType = "USER")
    public Result<?> postAdminUser(@RequestBody @Validated(UserDTO.CreateGroup.class) UserDTO dto) {
        userService.postAdminUser(dto);
        return Result.success("添加成功", null);
    }

    @PutMapping("/{userId}")
    @OperationLog(eventType = "ADMIN", action = "UPDATE_USER", resourceType = "USER", resourceIdParam = "userId")
    public Result<?> putAdminUser(@PathVariable Long userId,
                                  @RequestBody @Validated(UserDTO.UpdateGroup.class) UserDTO dto) {
        userService.putAdminUser(userId, dto, SecurityContextUtil.currentUserId());
        return Result.success("更新成功", null);
    }

    @PutMapping("/{userId}/default-avatar")
    @OperationLog(eventType = "ADMIN", action = "RESET_AVATAR", resourceType = "USER", resourceIdParam = "userId")
    public Result<?> putAdminUserDefaultAvatar(@PathVariable Long userId) {
        userService.putAdminUserDefaultAvatar(userId);
        return Result.success("已设为默认头像", null);
    }

    @PutMapping("/{userId}/recalculate-used-space")
    @OperationLog(eventType = "ADMIN", action = "RECALCULATE_SPACE", resourceType = "USER", resourceIdParam = "userId")
    public Result<?> putAdminUserRecalculateUsedSpace(@PathVariable Long userId) {
        userService.putAdminUserRecalculateUsedSpace(userId);
        return Result.success("已重新计算用户已用空间", null);
    }

    @DeleteMapping("/{userId}")
    @OperationLog(eventType = "ADMIN", action = "DELETE_USER", resourceType = "USER", resourceIdParam = "userId")
    public Result<?> deleteAdminUser(@PathVariable Long userId) {
        userService.deleteAdminUser(userId);
        return Result.success("删除成功", null);
    }
}
