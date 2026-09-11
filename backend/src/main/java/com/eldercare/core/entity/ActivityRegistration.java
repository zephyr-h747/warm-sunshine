package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动报名实体（对应 activity_registration 表，uk_user_activity 唯一索引防重复报名）
 */
@Data
public class ActivityRegistration implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 报名用户 ID */
    private Long userId;
    /** 报名活动 ID（关联 community_activity） */
    private Long activityId;
    /** 签到状态：NOT_CHECKED_IN 未签到/CHECKED_IN 已签到 */
    private String checkInStatus;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;
}
