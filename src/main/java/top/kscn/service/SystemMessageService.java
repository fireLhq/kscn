package top.kscn.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.kscn.entity.SystemMessage;
import top.kscn.entity.dto.systemMessage.SystemMessageManageDTO;
import top.kscn.entity.dto.systemMessage.SystemMessagePopupDismissDTO;
import top.kscn.entity.dto.systemMessage.SystemMessageQueryDTO;

import java.util.List;
import java.util.Map;

public interface SystemMessageService {

    Page<SystemMessage> getSystemMessagePage(SystemMessageQueryDTO dto);

    Page<SystemMessage> getSystemMessagePage(SystemMessageQueryDTO dto, boolean onlyActive);

    List<SystemMessage> getActivePopupMessages(Long userId, String browserId);

    void dismissPopupMessage(Long systemMessageId, Long userId, SystemMessagePopupDismissDTO dto);

    Map<String, Object> getHomeSystemMessageList(Integer page, Integer size);

    void postSystemMessage(SystemMessageManageDTO dto, Long senderId);

    void putSystemMessage(Long systemMessageId, SystemMessageManageDTO dto, Long senderId);

    void deleteSystemMessage(Long systemMessageId);
}
