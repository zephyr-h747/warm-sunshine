package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 体检套餐 VO（items 已解析为字符串列表）
 */
@Data
public class PackageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 套餐 ID */
    private Long id;
    /** 套餐名称 */
    private String name;
    /** 套餐封面图 URL */
    private String coverUrl;
    /** 套餐简介 */
    private String description;
    /** 价格（积分抵扣） */
    private Integer price;
    /** 适用人群说明 */
    private String suitablePeople;
    /** 包含项目列表 */
    private List<String> items;
    /** 状态：ENABLED 启用/DISABLED 禁用 */
    private String status;

}
