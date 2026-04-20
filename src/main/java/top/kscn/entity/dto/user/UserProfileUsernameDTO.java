package top.kscn.entity.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileUsernameDTO {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]{3,15}$", message = "用户名不合法，必须由字母、下划线、数字组成，且长度在4-16之间，不能以数字开头")
    private String username;
}

