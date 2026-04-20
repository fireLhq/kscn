package top.kscn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.kscn.common.Result;
import top.kscn.entity.SystemMessage;
import top.kscn.entity.dto.systemMessage.SystemMessageManageDTO;
import top.kscn.entity.dto.systemMessage.SystemMessageQueryDTO;
import top.kscn.service.SystemMessageService;
import top.kscn.utils.SecurityContextUtil;

@RestController
@RequestMapping("/api/admin/system-message")
public class AdminSystemMessageController {

    @Autowired
    private SystemMessageService systemMessageService;

    @GetMapping
    public Result<?> getAdminSystemMessagePage(SystemMessageQueryDTO dto,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size) {
        dto.setPage(page != null ? page : dto.getPage());
        dto.setSize(size != null ? size : dto.getSize());
        Page<SystemMessage> pageData = systemMessageService.getSystemMessagePage(dto, false);
        return Result.success(null, pageData);
    }

    @PostMapping
    public Result<?> postSystemMessage(@RequestBody @Validated(SystemMessageManageDTO.CreateGroup.class) SystemMessageManageDTO dto) {
        systemMessageService.postSystemMessage(dto, SecurityContextUtil.currentUserId());
        return Result.success("发布成功", null);
    }

    @PutMapping("/{systemMessageId}")
    public Result<?> putSystemMessage(@PathVariable Long systemMessageId,
                                      @RequestBody @Validated(SystemMessageManageDTO.UpdateGroup.class) SystemMessageManageDTO dto) {
        systemMessageService.putSystemMessage(systemMessageId, dto, SecurityContextUtil.currentUserId());
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{systemMessageId}")
    public Result<?> deleteSystemMessage(@PathVariable Long systemMessageId) {
        systemMessageService.deleteSystemMessage(systemMessageId);
        return Result.success("删除成功", null);
    }
}

