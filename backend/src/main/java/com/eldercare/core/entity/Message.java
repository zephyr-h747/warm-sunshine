package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体（对应 message 表）
 */
@Data
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 接收消息的用户 ID */
    private Long userId;
    /** 消息标题 */
    private String title;
    /** 消息内容 */
    private String content;
    /** 类型：APPOINTMENT 预约/ACTIVITY 活动/SYSTEM 系统/HEALTH_REMINDER 健康提醒 */
    private String type;
    /** 是否已读：0 未读/1 已读 */
    private Integer isRead;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;


}
