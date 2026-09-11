package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 会话 VO（创建返回 + 列表项，列表含最后一条消息预览）
 */
@Data
public class SessionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话 ID */
    private Long id;
    /** 会话名称 */
    private String sessionName;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 最后一条消息预览（仅列表返回） */
    private String lastMessage;
}
