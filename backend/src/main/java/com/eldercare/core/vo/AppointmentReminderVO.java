package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 预约提醒 VO（明日待完成预约短信推送用）
 */
@Data
public class AppointmentReminderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 预约 ID */
    private Long appointmentId;
    /** 预约用户 ID */
    private Long userId;
    /** 用户手机号（短信接收方） */
    private String phone;
    /** 体检套餐名称 */
    private String packageName;
    /** 预约日期 */
    private LocalDate appointDate;
    /** 预约时间段 */
    private String timeRange;

}
