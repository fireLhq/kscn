package top.kscn.entity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.*;

@Data
public class FileOperationRequest {
    @NotBlank @Pattern(regexp = "COPY|MOVE|RENAME")
    private String action;
    @Pattern(regexp = "PUBLIC|USER")
    private String targetSpace; // null 为当前空间；跨空间只支持复制，用户身份由服务端确定。
    @NotEmpty @Size(max = 200)
    private List<@NotNull @Positive Long> fileIds;
    @Positive
    private Long targetParentId;
    @NotNull @Pattern(regexp = "ERROR|SKIP|AUTO")
    private String conflictPolicy = "ERROR";
    @Size(max = 255)
    private String baseName;
    @NotNull @Pattern(regexp = "PLAIN|PARENTHESES|DASH|UNDERSCORE|PADDED")
    private String nameStyle = "PLAIN";
    @Min(1) @Max(8)
    private int digits = 1;
    @NotNull @Size(max = 200)
    private Map<Long, @NotBlank @Size(max = 255) String> customNames = new HashMap<>();
    @Size(max = 64)
    private String previewToken;
}
