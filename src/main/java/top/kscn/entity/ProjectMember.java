package top.kscn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.kscn.handler.JsonListTypeHandler;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "project_member", autoResultMap = true)
public class ProjectMember {

    @TableId(value = "member_id", type = IdType.AUTO)
    private Long memberId;       // 项目成员ID

    private Long userId;         // 关联的用户ID（外键）

    private String role;         // 项目角色（如前端开发工程师、项目负责人）

    private String description;  // 个人描述

    // JSON 转 List<String>
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> skills;       // 技能列表（存JSON字符串）

    private Integer type;        // 类型（0-开发人员，1-管理人员）

    private LocalDateTime createTime;  // 创建时间

    private LocalDateTime updateTime;  // 更新时间
}
