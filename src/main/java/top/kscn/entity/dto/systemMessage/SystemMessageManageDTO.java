package top.kscn.entity.dto.systemMessage;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class SystemMessageManageDTO {

    public interface CreateGroup {}
    public interface UpdateGroup {}

    @NotBlank(message = "消息标题不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "消息标题长度不能超过100个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String title;

    @NotBlank(message = "消息内容不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(max = 5000, message = "消息内容长度不能超过5000个字符", groups = {CreateGroup.class, UpdateGroup.class})
    private String content;

    @Min(value = 0, message = "弹窗参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    @Max(value = 1, message = "弹窗参数错误", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer isPopup;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date popupStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date popupEndTime;
}

