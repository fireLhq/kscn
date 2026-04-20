package top.kscn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.kscn.entity.SystemMessage;
import top.kscn.entity.SystemMessagePopupRecord;
import top.kscn.entity.dto.systemMessage.SystemMessageManageDTO;
import top.kscn.entity.dto.systemMessage.SystemMessagePopupDismissDTO;
import top.kscn.entity.dto.systemMessage.SystemMessageQueryDTO;
import top.kscn.exception.CustomException;
import top.kscn.mapper.SystemMessageMapper;
import top.kscn.mapper.SystemMessagePopupRecordMapper;
import top.kscn.service.SystemMessageService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SystemMessageServiceImpl implements SystemMessageService {

    @Autowired
    private SystemMessageMapper systemMessageMapper;

    @Autowired
    private SystemMessagePopupRecordMapper systemMessagePopupRecordMapper;

    @Override
    public Page<SystemMessage> getSystemMessagePage(SystemMessageQueryDTO dto) {
        return getSystemMessagePage(dto, true);
    }

    @Override
    public Page<SystemMessage> getSystemMessagePage(SystemMessageQueryDTO dto, boolean onlyActive) {
        int pageNum = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int pageSize = dto.getSize() == null || dto.getSize() < 1 ? 10 : Math.min(dto.getSize(), 50);
        Date now = dto.getNowTime() == null ? new Date() : dto.getNowTime();

        LambdaQueryWrapper<SystemMessage> wrapper = new LambdaQueryWrapper<SystemMessage>()
                .eq(SystemMessage::getIsDeleted, 0)
                .like(dto.getKeyword() != null && !dto.getKeyword().isBlank(), SystemMessage::getTitle, dto.getKeyword())
                .eq(dto.getIsPopup() != null, SystemMessage::getIsPopup, dto.getIsPopup())
                .orderByDesc(SystemMessage::getCreateTime)
                .orderByDesc(SystemMessage::getId);

        if (onlyActive) {
            wrapper.le(SystemMessage::getCreateTime, now);
        }

        Page<SystemMessage> page = new Page<>(pageNum, pageSize);
        return systemMessageMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getHomeSystemMessageList(Integer page, Integer size) {
        SystemMessageQueryDTO dto = new SystemMessageQueryDTO();
        dto.setPage(page);
        dto.setSize(size);
        dto.setNowTime(new Date());
        Page<SystemMessage> pageData = getSystemMessagePage(dto, true);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData.getRecords());
        result.put("total", pageData.getTotal());
        result.put("current", pageData.getCurrent());
        result.put("size", pageData.getSize());
        result.put("pages", pageData.getPages());
        result.put("hasMore", pageData.getCurrent() < pageData.getPages());
        return result;
    }

    @Override
    public List<SystemMessage> getActivePopupMessages(Long userId, String browserId) {
        Date now = new Date();
        List<SystemMessage> popupMessages = systemMessageMapper.selectList(new LambdaQueryWrapper<SystemMessage>()
                .eq(SystemMessage::getIsDeleted, 0)
                .eq(SystemMessage::getIsPopup, 1)
                .le(SystemMessage::getCreateTime, now)
                .and(wrapper -> wrapper
                        .isNull(SystemMessage::getPopupStartTime)
                        .or().le(SystemMessage::getPopupStartTime, now))
                .and(wrapper -> wrapper
                        .isNull(SystemMessage::getPopupEndTime)
                        .or().ge(SystemMessage::getPopupEndTime, now))
                .orderByDesc(SystemMessage::getCreateTime)
                .orderByDesc(SystemMessage::getId));

        if (userId == null || browserId == null || browserId.isBlank()) {
            return popupMessages;
        }

        Set<Long> hiddenMessageIds = systemMessagePopupRecordMapper.selectList(new LambdaQueryWrapper<SystemMessagePopupRecord>()
                        .eq(SystemMessagePopupRecord::getUserId, userId)
                        .eq(SystemMessagePopupRecord::getBrowserId, browserId)
                        .eq(SystemMessagePopupRecord::getIsNoLongerPrompt, 1))
                .stream()
                .map(SystemMessagePopupRecord::getSystemMessageId)
                .collect(Collectors.toSet());

        if (hiddenMessageIds.isEmpty()) {
            return popupMessages;
        }

        return popupMessages.stream()
                .filter(message -> !hiddenMessageIds.contains(message.getId()))
                .toList();
    }

    @Override
    @Transactional
    public void dismissPopupMessage(Long systemMessageId, Long userId, SystemMessagePopupDismissDTO dto) {
        if (userId == null) throw new CustomException(401, "请先登录");
        if (dto.getBrowserId() == null || dto.getBrowserId().isBlank()) {
            throw new CustomException(400, "浏览器标识不能为空");
        }

        SystemMessage message = systemMessageMapper.selectById(systemMessageId);
        if (message == null || Integer.valueOf(1).equals(message.getIsDeleted())) {
            throw new CustomException(404, "系统消息不存在");
        }
        if (!Integer.valueOf(1).equals(message.getIsPopup())) {
            throw new CustomException(400, "该消息未开启弹窗提醒");
        }

        Date now = new Date();
        SystemMessagePopupRecord record = systemMessagePopupRecordMapper.selectOne(new LambdaQueryWrapper<SystemMessagePopupRecord>()
                .eq(SystemMessagePopupRecord::getSystemMessageId, systemMessageId)
                .eq(SystemMessagePopupRecord::getUserId, userId)
                .eq(SystemMessagePopupRecord::getBrowserId, dto.getBrowserId())
                .last("limit 1"));

        if (record == null) {
            record = new SystemMessagePopupRecord();
            record.setSystemMessageId(systemMessageId);
            record.setUserId(userId);
            record.setBrowserId(dto.getBrowserId());
            record.setIsNoLongerPrompt(1);
            record.setDismissTime(now);
            if (systemMessagePopupRecordMapper.insert(record) <= 0) {
                throw new CustomException(500, "记录不再提示失败");
            }
            return;
        }

        record.setIsNoLongerPrompt(1);
        record.setDismissTime(now);
        if (systemMessagePopupRecordMapper.updateById(record) <= 0) {
            throw new CustomException(500, "记录不再提示失败");
        }
    }

    @Override
    @Transactional
    public void postSystemMessage(SystemMessageManageDTO dto, Long senderId) {
        validatePopupRange(dto);

        SystemMessage message = new SystemMessage();
        message.setSenderId(senderId);
        message.setTitle(dto.getTitle().trim());
        message.setContent(dto.getContent().trim());
        message.setIsPopup(dto.getIsPopup() == null ? 0 : dto.getIsPopup());
        message.setPopupStartTime(dto.getPopupStartTime());
        message.setPopupEndTime(dto.getPopupEndTime());
        message.setIsDeleted(0);
        if (systemMessageMapper.insert(message) <= 0) {
            throw new CustomException(500, "发布系统消息失败");
        }
    }

    @Override
    @Transactional
    public void putSystemMessage(Long systemMessageId, SystemMessageManageDTO dto, Long senderId) {
        validatePopupRange(dto);

        SystemMessage message = systemMessageMapper.selectById(systemMessageId);
        if (message == null || Integer.valueOf(1).equals(message.getIsDeleted())) {
            throw new CustomException(404, "系统消息不存在");
        }

        message.setSenderId(senderId);
        message.setTitle(dto.getTitle().trim());
        message.setContent(dto.getContent().trim());
        message.setIsPopup(dto.getIsPopup() == null ? 0 : dto.getIsPopup());
        message.setPopupStartTime(dto.getPopupStartTime());
        message.setPopupEndTime(dto.getPopupEndTime());
        if (systemMessageMapper.updateById(message) <= 0) {
            throw new CustomException(500, "更新系统消息失败");
        }
    }

    @Override
    @Transactional
    public void deleteSystemMessage(Long systemMessageId) {
        SystemMessage message = systemMessageMapper.selectById(systemMessageId);
        if (message == null || Integer.valueOf(1).equals(message.getIsDeleted())) {
            throw new CustomException(404, "系统消息不存在");
        }

        message.setIsDeleted(1);
        message.setUpdateTime(new Date());
        if (systemMessageMapper.updateById(message) <= 0) {
            throw new CustomException(500, "删除系统消息失败");
        }
    }

    private void validatePopupRange(SystemMessageManageDTO dto) {
        Integer isPopup = dto.getIsPopup() == null ? 0 : dto.getIsPopup();
        if (!Integer.valueOf(1).equals(isPopup)) {
            dto.setPopupStartTime(null);
            dto.setPopupEndTime(null);
            return;
        }
        if (dto.getPopupStartTime() != null && dto.getPopupEndTime() != null
                && dto.getPopupStartTime().after(dto.getPopupEndTime())) {
            throw new CustomException(400, "弹窗开始时间不能晚于结束时间");
        }
    }
}
