package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 健康趋势 VO：某指标的近 6 个月统计与逐日数据
 */
@Data
public class HealthTrendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 指标 key：SYSTOLIC/DIASTOLIC/BLOOD_SUGAR/HEART_RATE/WEIGHT/BMI */
    private String indicator;
    /** 指标名称：收缩压/舒张压/血糖/心率/体重/BMI */
    private String indicatorName;
    /** 近 6 个月平均值 */
    private BigDecimal avgValue;
    /** 近 6 个月最大值 */
    private BigDecimal maxValue;
    /** 近 6 个月最小值 */
    private BigDecimal minValue;
    /** 逐日数据点列表（按时间升序） */
    private List<TrendRecord> records;

    /**
     * 单日数据点
     */
    @Data
    public static class TrendRecord implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 日期 yyyy-MM-dd */
        private String date;
        /** 当日指标值 */
        private BigDecimal value;

        public TrendRecord() {
        }

        public TrendRecord(String date, BigDecimal value) {
            this.date = date;
            this.value = value;
        }

    }
}
