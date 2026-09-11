package com.eldercare.api.controller;

import com.eldercare.api.service.MessageService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.util.SecurityUtils;
import com.eldercare.core.vo.MessageVO;
import com.eldercare.core.vo.UnreadCountVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【用户端】消息通知接口（/api/member/message）
 */
@RestController
@RequestMapping("/api/member/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** 消息列表（?type=&pageNum=&pageSize=） */
    @GetMapping("/list")
    public Result<PageResult<MessageVO>> list(@RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize,
                                              @RequestParam(required = false) String type) {
        return Result.success(messageService.list(SecurityUtils.getCurrentUserId(), pageNum, pageSize, type));
    }

    /** 消息详情 */
    @GetMapping("/{id}")
    public Result<MessageVO> detail(@PathVariable Long id) {
        return Result.success(messageService.detail(SecurityUtils.getCurrentUserId(), id));
    }

    /** 标记已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(SecurityUtils.getCurrentUserId(), id);
        return Result.success("已标记为已读", null);
    }

    /** 未读统计 */
    @GetMapping("/unread-count")
    public Result<UnreadCountVO> unreadCount() {
        return Result.success(messageService.unreadCount(SecurityUtils.getCurrentUserId()));
    }
}
