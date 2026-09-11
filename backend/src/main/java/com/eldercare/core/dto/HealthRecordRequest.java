package com.eldercare.core.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 健康记录录入请求（各项指标均选填，至少填一项）
 */
@Data
public class HealthRecordRequest {

    /** 收缩压（mmHg） */
    @Min(value = 30, message = "收缩压范围不正确")
    @Max(value = 250, message = "收缩压范围不正确")
    private Integer systolic;

    /** 舒张压（mmHg） */
    @Min(value = 20, message = "舒张压范围不正确")
    @Max(value = 200, message = "舒张压范围不正确")
    private Integer diastolic;

    /** 血糖（mmol/L） */
    @DecimalMin(value = "1.0", message = "血糖范围不正确")
    @DecimalMax(value = "30.0", message = "血糖范围不正确")
    private BigDecimal bloodSugar;

    /** 心率（次/分） */
    @Min(value = 30, message = "心率范围不正确")
    @Max(value = 250, message = "心率范围不正确")
    private Integer heartRate;

    /** 体重（kg） */
    @DecimalMin(value = "20.0", message = "体重范围不正确")
    @DecimalMax(value = "300.0", message = "体重范围不正确")
    private BigDecimal weight;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过 500 字")
    private String memo;

}
