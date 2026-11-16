package top.kscn.entity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NetworkFileRequest {
    @NotBlank @Size(max = 8192) private String url;
    @NotBlank @Pattern(regexp = "IMPORT|LINK") private String mode;
    @Size(max = 255) private String fileName;
    @Positive private Long parentId;
}
