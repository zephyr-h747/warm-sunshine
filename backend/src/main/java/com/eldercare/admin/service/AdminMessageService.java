package com.eldercare.admin.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.dto.PushBatchMessageRequest;
import com.eldercare.core.dto.PushMessageRequest;
import com.eldercare.core.entity.Message;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.MessageMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.SmsService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 【管理端】消息管理服务：全站消息列表 + 单推/批量推送（站内 + 短信双通道）
 */
@Service
public class AdminMessageService {

    private static final Logger log = LoggerFactory.getLogger(AdminMessageService.class);

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final SmsService smsService;

    public AdminMessageService(MessageMapper messageMapper, UserMapper userMapper, SmsService smsService) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
        this.smsService = smsService;
    }

    /** 全站消息列表（分页 + 类型筛选） */
    public PageResult<Message> list(String type, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Message> list = messageMapper.selectAll(type);
        return PageResult.of(new PageInfo<>(list));
    }

    /** 推送单条消息（站内 + 可选短信） */
    public void push(PushMessageRequest req) {
        User user = userMapper.selectById(req.getUserId());
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        insertMessage(req.getUserId(), req.getTitle(), req.getContent(), req.getType());
        if (Boolean.TRUE.equals(req.getSms())) {
            sendSmsQuietly(user, req.getContent());
        }
        log.info("管理端推送消息: userId={}, type={}, sms={}", req.getUserId(), req.getType(), req.getSms());
    }

    /** 批量推送（多用户），返回成功条数 */
    public int pushBatch(PushBatchMessageRequest req) {
        int count = 0;
        for (Long userId : req.getUserIds()) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                continue;
            }
            insertMessage(userId, req.getTitle(), req.getContent(), req.getType());
            if (Boolean.TRUE.equals(req.getSms())) {
                sendSmsQuietly(user, req.getContent());
            }
            count++;
        }
        log.info("管理端批量推送消息: 目标={} 成功={}, type={}", req.getUserIds().size(), count, req.getType());
        return count;
    }

    private void insertMessage(Long userId, String title, String content, String type) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        messageMapper.insert(message);
    }

    private void sendSmsQuietly(User user, String content) {
        try {
            smsService.sendNotification(user.getPhone(), content);
        } catch (Exception e) {
            log.warn("短信发送失败: userId={}, err={}", user.getId(), e.getMessage());
        }
    }
}
