package top.kscn.entity.dto;

import lombok.Data;
import top.kscn.entity.User;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectMemberDTO {

    private Long memberId;
    private Long userId;

    private String role;
    private String description;
    private List<String> skills;
    private Integer type;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 分页参数
    private Integer page;
    private Integer size;

    // 用户信息
    private User user; // 直接嵌套 User 实体
}
