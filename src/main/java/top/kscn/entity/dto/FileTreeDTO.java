package top.kscn.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileTreeDTO {
    private String name;
    private String path;
    private List<FileTreeDTO> children;
}