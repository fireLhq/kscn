package top.kscn.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.kscn.common.Result;
import top.kscn.entity.SystemMessage;
import top.kscn.entity.dto.systemMessage.SystemMessagePopupDismissDTO;
import top.kscn.service.SystemMessageService;
import top.kscn.utils.SecurityContextUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-message")
public class SystemMessageController {

    @Autowired
    private SystemMessageService systemMessageService;

    @GetMapping("/home/list")
    public Result<?> getHomeSystemMessageList(@RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size) {
        Map<String, Object> data = systemMessageService.getHomeSystemMessageList(page, size);
        return Result.success(null, data);
    }

    @GetMapping("/popup/active")
    public Result<?> getActivePopupMessages(@RequestParam String browserId) {
        Long userId = SecurityContextUtil.currentUserId();
        List<SystemMessage> data = systemMessageService.getActivePopupMessages(userId, browserId);
        return Result.success(null, data);
    }

    @PostMapping("/popup/{systemMessageId}/dismiss")
    @PreAuthorize("isAuthenticated()")
    public Result<?> dismissPopupMessage(@PathVariable Long systemMessageId,
                                         @RequestBody @Valid SystemMessagePopupDismissDTO dto) {
        systemMessageService.dismissPopupMessage(systemMessageId, SecurityContextUtil.currentUserId(), dto);
        return Result.success("已记录不再提示", null);
    }
}
