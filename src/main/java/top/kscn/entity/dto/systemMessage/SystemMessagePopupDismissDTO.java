package top.kscn.entity.dto.systemMessage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemMessagePopupDismissDTO {

    @NotBlank(message = "浏览器标识不能为空")
    private String browserId;
}

