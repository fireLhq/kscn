package top.kscn.entity.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    public interface CreateGroup {}
    public interface UpdateGroup {}

    private Long userId; // 用户ID
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]{3,15}$", message = "用户名不合法，必须由字母、下划线、数字组成，且长度在4-16之间，不能以数字开头", groups = {CreateGroup.class, UpdateGroup.class})
    private String username; // 用户名
    @NotBlank(message = "邮箱不能为空", groups = CreateGroup.class)
    @Email(message = "邮箱格式不正确", groups = {CreateGroup.class, UpdateGroup.class})
    private String email; // 邮箱
    @NotBlank(message = "密码不能为空", groups = CreateGroup.class)
    @Size(min = 6, message = "密码长度不能少于6位", groups = CreateGroup.class)
    private String password; // 密码
    @Min(value = 0, message = "角色参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 1, message = "角色参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer role; // 角色[0:管理员, 1:普通用户]
    @Size(max = 10, message = "昵称长度不能超过10", groups = {CreateGroup.class, UpdateGroup.class})
    private String nickname; // 昵称
    private String avatar; // 头像
    @Min(value = 0, message = "性别参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 2, message = "性别参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer sex; // 性别[0:男 1:女 2:不愿透露]
    private Date birthday; // 生日
    @Size(max = 125, message = "签名长度不能超过125", groups = {CreateGroup.class, UpdateGroup.class})
    private String synopsis; // 签名
    @Min(value = 0, message = "状态参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 1, message = "状态参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer status; // 状态[0:禁用 1:正常]
    private Integer isDeleted; // 是否删除[0:否 1:是]
    private Date deleteTime; // 删除时间
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
    private Long totalSpace; // 总空间
    private Long usedSpace; // 已使用空间
}
