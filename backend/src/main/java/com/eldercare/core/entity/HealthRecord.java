package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康记录实体（对应 health_record 表）
 */

@Data
public class HealthRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 记录所属用户 ID */
    private Long userId;
    /** 收缩压（mmHg） */
    private Integer systolic;
    /** 舒张压（mmHg） */
    private Integer diastolic;
    /** 血糖（mmol/L） */
    private BigDecimal bloodSugar;
    /** 心率（次/分） */
    private Integer heartRate;
    /** 体重（kg） */
    private BigDecimal weight;
    /** BMI 指数 */
    private BigDecimal bmi;
    /** 备注 */
    private String memo;
    /** 记录时间 */
    private LocalDateTime recordedAt;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;


}
