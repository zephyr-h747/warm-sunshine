package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约时段实体（对应 appointment_slot 表）
 */
@Data
public class AppointmentSlot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 所属套餐 ID（关联 appointment_package） */
    private Long packageId;
    /** 预约日期 */
    private LocalDate appointDate;
    /** 时间段描述（如 09:00-10:00） */
    private String timeRange;
    /** 最大预约人数 */
    private Integer maxCount;
    /** 当前已预约人数 */
    private Integer currentCount;
    /** 状态：AVAILABLE 可预约/FULL 已满/CLOSED 已关闭 */
    private String status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;
}
