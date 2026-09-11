package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 管理端批量推送消息请求
 */
@Data
public class PushBatchMessageRequest {

    /** 接收消息的用户 ID 列表 */
    @NotEmpty(message = "用户列表不能为空")
    private List<Long> userIds;

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
