package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 对话发送消息请求
 */
@Data
public class SendMessageRequest {

    /** 会话 ID */
    @NotNull(message = "会话 ID 不能为空")
    private Long sessionId;

    /** 用户消息内容 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过 4000 字")
    private String message;
}
