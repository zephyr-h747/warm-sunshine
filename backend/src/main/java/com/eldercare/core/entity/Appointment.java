package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约实体（对应 appointment 表）
 */
@Data
public class Appointment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 预约用户 ID */
    private Long userId;
    /** 预约时段 ID（关联 appointment_slot） */
    private Long slotId;
    /** 体检套餐 ID（关联 appointment_package） */
    private Long packageId;
    /** 状态：PENDING 待确认/CONFIRMED 已确认/CANCELED 已取消/COMPLETED 已完成 */
    private String status;
    /** 体检报告 URL */
    private String reportUrl;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;
}
