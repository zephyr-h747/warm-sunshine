package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理端批量生成时段请求
 */
@Data
public class SlotGenerateRequest {

    /** 目标套餐 ID */
    @NotNull(message = "套餐 ID 不能为空")
    private Long packageId;

    /** 生成起始日期 */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /** 生成结束日期 */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /** 每日时段列表（如 09:00-10:00） */
    @NotEmpty(message = "时段列表不能为空")
    private List<@NotBlank(message = "时段不能为空") String> timeRanges;

    /** 每个时段最大预约人数 */
    @NotNull(message = "最大人数不能为空")
    @Positive(message = "最大人数必须为正数")
    private Integer maxCount;


}
