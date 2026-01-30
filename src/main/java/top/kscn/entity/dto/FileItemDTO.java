package top.kscn.entity.dto;

import lombok.Data;

@Data
public class FileItemDTO {
    private String name;        // 文件/文件夹名称
    private String path;        // 相对路径（相对 root）
    private String type;        // "file" or "folder"
    private Long size;          // 文件大小（字节数，文件夹为 null）
    private Long modifiedTime;  // 最后修改时间（时间戳）
    private Integer itemCount;  // 文件夹子项数量（文件为 null）
}
