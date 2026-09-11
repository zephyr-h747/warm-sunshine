package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端创建/编辑活动请求
 */
@Data
public class ActivityRequest {

    /** 活动标题 */
    @NotBlank(message = "活动标题不能为空")
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
    @NotNull(message = "最大人数不能为空")
    @Positive(message = "最大人数必须为正数")
    private Integer maxParticipants;

    /** 活动状态：DRAFT/REGISTRATING/IN_PROGRESS/ENDED/CANCELED */
    @Pattern(regexp = "DRAFT|REGISTRATING|IN_PROGRESS|ENDED|CANCELED", message = "活动状态不合法")
    private String status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(LocalDateTime registrationStart) { this.registrationStart = registrationStart; }
    public LocalDateTime getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(LocalDateTime registrationEnd) { this.registrationEnd = registrationEnd; }
    public LocalDateTime getActivityStart() { return activityStart; }
    public void setActivityStart(LocalDateTime activityStart) { this.activityStart = activityStart; }
    public LocalDateTime getActivityEnd() { return activityEnd; }
    public void setActivityEnd(LocalDateTime activityEnd) { this.activityEnd = activityEnd; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
