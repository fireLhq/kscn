package top.kscn.entity.dto.member;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberDTO {

    public interface CreateGroup {}
    public interface UpdateGroup {}

    private Long id;

    @NotBlank(message = "成员姓名不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(max = 50, message = "成员姓名长度不能超过50", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    @NotNull(message = "成员类型不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Min(value = 1, message = "成员类型参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 2, message = "成员类型参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer memberType;

    @NotBlank(message = "角色名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "角色名称长度不能超过100", groups = {CreateGroup.class, UpdateGroup.class})
    private String roleName;

    @Size(max = 500, message = "成员介绍长度不能超过500", groups = {CreateGroup.class, UpdateGroup.class})
    private String intro;

    private String avatar;

    @Size(max = 500, message = "个人主页长度不能超过500", groups = {CreateGroup.class, UpdateGroup.class})
    private String website;

    private Integer sort;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Long page;
    private Long size;
}
