package top.kscn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("system_message")
public class SystemMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long senderId;
    private String title;
    private String content;
    private Integer isPopup;
    private Date popupStartTime;
    private Date popupEndTime;
    private Integer isDeleted;
    private Date createTime;
    private Date updateTime;
}

