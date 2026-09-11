package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 对话消息 VO（AI 对话模块）
 */
@Data
public class ChatMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息 ID */
    private Long id;
    /** 角色：user 用户/assistant 助手 */
    private String role;
    /** 消息内容 */
    private String message;
    /** 创建时间 */
    private LocalDateTime createTime;
}
