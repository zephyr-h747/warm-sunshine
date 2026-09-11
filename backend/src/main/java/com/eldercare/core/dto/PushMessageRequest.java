package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理端推送单条消息请求
 */
@Data
public class PushMessageRequest {

    /** 接收消息的用户 ID */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /** 消息标题 */
    @NotBlank(message = "标题不能为空")
    private String title;

    /** 消息内容 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 消息类型：APPOINTMENT/ACTIVITY/SYSTEM/HEALTH_REMINDER */
    @NotBlank(message = "消息类型不能为空")
    @Pattern(regexp = "APPOINTMENT|ACTIVITY|SYSTEM|HEALTH_REMINDER", message = "消息类型不合法")
    private String type;

    /** 是否同时发短信 */
    private Boolean sms = false;


}
