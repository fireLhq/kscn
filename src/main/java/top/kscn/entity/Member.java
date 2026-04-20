package top.kscn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("member")
public class Member {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer memberType;

    private String roleName;

    private String intro;

    private String avatar;

    private String website;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

