package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 预约时段 VO（含剩余名额）
 */
@Data
public class SlotVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 时段 ID */
    private Long id;
    /** 所属套餐 ID */
    private Long packageId;
    /** 预约日期 */
    private LocalDate appointDate;
    /** 时间段描述（如 09:00-10:00） */
    private String timeRange;
    /** 最大预约人数 */
    private Integer maxCount;
    /** 当前已预约人数 */
    private Integer currentCount;
    /** 剩余名额 = max_count - current_count */
    private Integer remaining;
    /** 状态：AVAILABLE 可预约/FULL 已满/CLOSED 已关闭 */
    private String status;


}
