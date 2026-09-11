package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理端预约列表 VO（含用户信息）
 */
@Data
public class AdminAppointmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 预约 ID */
    private Long id;
    /** 预约用户 ID */
    private Long userId;
    /** 用户手机号 */
    private String userPhone;
    /** 用户真实姓名 */
    private String userName;
    /** 体检套餐名称 */
    private String packageName;
    /** 套餐价格（积分） */
    private Integer price;
    /** 预约日期 */
    private LocalDate appointDate;
    /** 预约时间段 */
    private String timeRange;
    /** 预约状态：PENDING/CONFIRMED/CANCELED/COMPLETED */
    private String status;
    /** 体检报告 URL */
    private String reportUrl;
    /** 创建时间 */
    private LocalDateTime createTime;


}
