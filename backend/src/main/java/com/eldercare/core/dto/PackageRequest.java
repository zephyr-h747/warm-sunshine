package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 管理端创建/编辑体检套餐请求
 */
@Data
public class PackageRequest {

    /** 套餐名称 */
    @NotBlank(message = "套餐名称不能为空")
    @Size(max = 100, message = "套餐名称过长")
    private String name;

    /** 套餐封面图 URL */
    private String coverUrl;

    /** 套餐简介 */
    private String description;

    /** 价格（积分抵扣） */
    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须为正数")
    private Integer price;

    /** 适用人群说明 */
    private String suitablePeople;

    /** 包含项目 */
    private List<String> items;

    /** 状态：ENABLED 启用/DISABLED 禁用 */
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态取值不合法")
    private String status;


}
