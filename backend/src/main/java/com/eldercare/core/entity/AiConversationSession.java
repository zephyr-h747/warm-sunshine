package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 会话实体（对应 ai_conversation_session 表）
 */
@Data
public class AiConversationSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 会话名称 */
    private String sessionName;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;
}
