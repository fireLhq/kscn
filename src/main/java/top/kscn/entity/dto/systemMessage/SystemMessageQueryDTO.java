package top.kscn.entity.dto.systemMessage;

import lombok.Data;

import java.util.Date;

@Data
public class SystemMessageQueryDTO {
    private Integer page;
    private Integer size;
    private String keyword;
    private Integer isPopup;
    private Date nowTime;
}

