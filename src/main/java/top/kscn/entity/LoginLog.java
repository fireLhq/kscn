package top.kscn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("login_log")
public class LoginLog {
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
    private Long userId;
    private Date loginTime;
    private String ip;
    private String userAgent;
    private String deviceType;
    private String os;
    private String browser;
    private Integer status;
    private String remark;
}

