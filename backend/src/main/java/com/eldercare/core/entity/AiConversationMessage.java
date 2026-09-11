package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 对话消息实体（对应 ai_conversation_message 表）
 * <p>
 * 表结构无 status 字段：AI 失败时 role 仍为 assistant，message 内容存错误描述
 */
@Data
public class AiConversationMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息 ID */
    private Long id;
    /** 会话 ID */
    private Long sessionId;
    /** 用户 ID */
    private Long userId;
    /** 角色：user 用户/assistant 助手 */
    private String role;
    /** 消息内容 */
    private String message;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;
}
