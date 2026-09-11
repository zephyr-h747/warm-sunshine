package com.eldercare.api.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.Message;
import com.eldercare.core.exception.AccessDeniedException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.MessageMapper;
import com.eldercare.core.vo.MessageVO;
import com.eldercare.core.vo.UnreadCountVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 【用户端】消息通知服务
 */
@Service
public class MessageService {

    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    /** 消息列表（分页 + 可选类型筛选） */
    public PageResult<MessageVO> list(Long userId, int pageNum, int pageSize, String type) {
        PageHelper.startPage(pageNum, pageSize);
        List<Message> list = messageMapper.selectByUserId(userId, type);
        PageInfo<Message> pageInfo = new PageInfo<>(list);
        List<MessageVO> vos = list.stream().map(this::toMessageVO).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    /** 消息详情（校验归属） */
    public MessageVO detail(Long userId, Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new ResourceNotFoundException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new AccessDeniedException("无权查看他人的消息");
        }
        return toMessageVO(message);
    }

    /** 标记已读 */
    public void markAsRead(Long userId, Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new ResourceNotFoundException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new AccessDeniedException("无权操作他人的消息");
        }
        if (message.getIsRead() == null || message.getIsRead() == 0) {
            messageMapper.updateIsRead(id, 1);
        }
    }

    /** 未读数量 */
    public UnreadCountVO unreadCount(Long userId) {
        return new UnreadCountVO(messageMapper.countUnread(userId));
    }

    private MessageVO toMessageVO(Message message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}
