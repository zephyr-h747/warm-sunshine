package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 每日健康数据统计 VO（定时汇总用，对应聚合查询结果）
 */
@Data
public class DailyHealthStatVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 记录数 */
    private Long recordCount;
    /** 平均收缩压（mmHg） */
    private Double avgSystolic;
    /** 平均舒张压（mmHg） */
    private Double avgDiastolic;
    /** 平均血糖（mmol/L） */
    private Double avgBloodSugar;
    /** 平均心率（次/分） */
    private Double avgHeartRate;


}
