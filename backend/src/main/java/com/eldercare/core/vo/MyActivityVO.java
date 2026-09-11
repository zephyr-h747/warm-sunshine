package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的活动 VO（活动信息 + 签到状态）
 */
@Data
public class MyActivityVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 活动 ID */
    private Long id;
    /** 活动标题 */
    private String title;
    /** 活动封面图 URL */
    private String coverUrl;
    /** 活动开始时间 */
    private LocalDateTime activityStart;
    /** 活动结束时间 */
    private LocalDateTime activityEnd;
    /** 活动状态：DRAFT/REGISTRATING/IN_PROGRESS/ENDED/CANCELED */
    private String status;
    /** 签到状态：NOT_CHECKED_IN/CHECKED_IN */
    private String checkInStatus;
    /** 报名时间 */
    private LocalDateTime createTime;

}
