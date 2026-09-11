package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未读消息统计 VO
 */
@Data
public class UnreadCountVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 未读消息数 */
    private long unreadCount;

    public UnreadCountVO(long unreadCount) {
        this.unreadCount = unreadCount;
    }


}
