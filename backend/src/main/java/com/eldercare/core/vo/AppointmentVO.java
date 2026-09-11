package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约 VO（含套餐名、时段、状态）
 */
@Data
public class AppointmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 预约 ID */
    private Long id;
    /** 体检套餐 ID */
    private Long packageId;
    /** 套餐名称 */
    private String packageName;
    /** 预约时段 ID */
    private Long slotId;
    /** 预约日期 */
    private LocalDate appointDate;
    /** 预约时间段 */
    private String timeRange;
    /** 套餐价格（积分） */
    private Integer price;
    /** 预约状态：PENDING/CONFIRMED/CANCELED/COMPLETED */
    private String status;
    /** 体检报告 URL */
    private String reportUrl;
    /** 创建时间 */
    private LocalDateTime createTime;
}
