package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动 VO（含剩余名额）
 */
@Data
public class ActivityVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 活动 ID */
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
    /** 剩余名额 = max - current */
    private Integer remaining;
    /** 活动状态：DRAFT/REGISTRATING/IN_PROGRESS/ENDED/CANCELED */
    private String status;
    /** 当前用户是否已报名 */
    private Boolean registered;
    /** 当前用户签到状态：NOT_CHECKED_IN/CHECKED_IN（未报名为 null） */
    private String checkInStatus;

}
