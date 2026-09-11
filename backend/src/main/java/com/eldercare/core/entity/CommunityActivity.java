package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社区活动实体（对应 community_activity 表）
 */
@Data
public class CommunityActivity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 活动标题 */
    private String title;
    /** 活动封面图 URL */
    private String coverUrl;
    /** 活动详情内容 */
    private String content;
    /** 报名开始时间 */
    private LocalDateTime registrationStart;
    /** 报名截止时间 */
    private LocalDateTime registrationEnd;
    /** 活动开始时间 */
    private LocalDateTime activityStart;
    /** 活动结束时间 */
    private LocalDateTime activityEnd;
    /** 最大参与人数 */
    private Integer maxParticipants;
    /** 当前参与人数 */
    private Integer currentParticipants;
    /** 状态：DRAFT 草稿/REGISTRATING 报名中/IN_PROGRESS 进行中/ENDED 已结束/CANCELED 已取消 */
    private String status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;
}
