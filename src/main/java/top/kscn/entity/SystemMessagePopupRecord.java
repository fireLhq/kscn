package top.kscn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("system_message_popup_record")
public class SystemMessagePopupRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long systemMessageId;
    private Long userId;
    private String browserId;
    private Integer isNoLongerPrompt;
    private Date dismissTime;
    private Date createTime;
    private Date updateTime;
}

