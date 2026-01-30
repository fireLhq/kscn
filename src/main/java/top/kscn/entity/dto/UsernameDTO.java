package top.kscn.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsernameDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
}
