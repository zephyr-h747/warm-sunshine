package com.eldercare.api.controller;

import com.eldercare.api.service.ChatService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.CreateSessionRequest;
import com.eldercare.core.dto.SendMessageRequest;
import com.eldercare.core.util.SecurityUtils;
import com.eldercare.core.vo.ChatMessageVO;
import com.eldercare.core.vo.SessionVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 【用户端】AI 对话接口（/api/member/chat），核心为 SSE 流式响应
 */
@RestController
@RequestMapping("/api/member/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** 创建会话 */
    @PostMapping("/session")
    public Result<SessionVO> createSession(@Valid @RequestBody CreateSessionRequest req) {
        return Result.success(chatService.createSession(SecurityUtils.getCurrentUserId(), req));
    }

    /** 会话列表（含最后一条消息预览） */
    @GetMapping("/sessions")
    public Result<PageResult<SessionVO>> sessions(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(chatService.listSessions(SecurityUtils.getCurrentUserId(), pageNum, pageSize));
    }

    /** 删除会话 */
    @DeleteMapping("/session/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        chatService.deleteSession(SecurityUtils.getCurrentUserId(), id);
        return Result.success();
    }

    /** 对话历史 */
    @GetMapping("/history/{id}")
    public Result<PageResult<ChatMessageVO>> history(@PathVariable Long id,
                                                     @RequestParam(defaultValue = "1") int pageNum,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(chatService.getHistory(SecurityUtils.getCurrentUserId(), id, pageNum, pageSize));
    }

    /**
     * 发送消息（SSE 流式响应）
     * <p>
     * 事件格式：data: {token}\n\n，结束事件：data: [DONE]\n\n；超时 60 秒
     */
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@Valid @RequestBody SendMessageRequest req) {
        return chatService.streamChat(SecurityUtils.getCurrentUserId(), req.getSessionId(), req.getMessage());
    }
}
