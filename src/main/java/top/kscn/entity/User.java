package top.kscn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "user")
public class User {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId; // 用户ID
    private String username; // 用户名
    private String email; // 邮箱
    private String password; // 密码
    private Integer role; // 角色[0:管理员, 1:普通用户]
    private String nickname; // 昵称
    private String avatar; // 头像
    private Integer sex; // 性别[0:男 1:女 2:不愿透露]
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday; // 生日
    private String synopsis; // 签名
    private Integer status; // 状态[0:禁用 1:正常]
    private Integer isDeleted; // 是否删除[0:否 1:是]
    private Date deleteTime; // 删除时间
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
    private Long totalSpace; // 总空间
    private Long usedSpace; // 已使用空间
}
