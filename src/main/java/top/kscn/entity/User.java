package top.kscn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "user")
public class User {
    /**
     * 用户ID，主键
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名，唯一，可以为空
     */
    private String username;

    /**
     * 邮箱，唯一，不可为空
     */
    private String email;

    /**
     * 密码，不可为空
     */
    private String password;

    /**
     * 角色：0-管理员，1-普通用户
     */
    private Integer role;

    /**
     * 昵称，可以为空
     */
    private String nickname;

    /**
     * 头像URL，可以为空
     */
    private String avatar;

    /**
     * 性别：0-男，1-女，2-不愿透露
     */
    private Integer sex;

    /**
     * 生日，可以为空
     */
    private Date birthday;

    /**
     * 说明，可以为空
     */
    private String synopsis;

    /**
     * 状态：0-未启用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
