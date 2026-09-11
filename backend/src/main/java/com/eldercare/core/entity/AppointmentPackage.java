package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 体检套餐实体（对应 appointment_package 表）
 */
@Data
public class AppointmentPackage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
    /** 包含项目 JSON 数组 */
    private String items;
    /** 状态：ENABLED/DISABLED */
    private String status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;
}
