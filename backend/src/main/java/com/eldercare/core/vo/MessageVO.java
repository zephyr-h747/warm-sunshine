package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息 VO（消息通知模块）
 */
@Data
public class MessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息 ID */
    private Long id;
    /** 消息标题 */
    private String title;
    /** 消息内容 */
    private String content;
    /** 消息类型：APPOINTMENT 预约/ACTIVITY 活动/SYSTEM 系统/HEALTH_REMINDER 健康提醒 */
    private String type;
    /** 是否已读：0 未读/1 已读 */
    private Integer isRead;
    /** 创建时间 */
    private LocalDateTime createTime;
}
