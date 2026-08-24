package top.kscn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("log")
public class Log {
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
    private Date eventTime;
    private String eventType;
    private String action;
    private Long userId;
    private String ip;
    private String userAgent;
    private String deviceType;
    private String os;
    private String browser;
    private String httpMethod;
    private String requestPath;
    private Integer httpStatus;
    private String resourceType;
    private Long resourceId;
    private String resourceName;
    private Integer status;
    private String remark;
    private String detailsJson;
}