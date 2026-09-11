package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体（对应 sys_config 表）
 */
@Data
public class SysConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 配置键 */
    private String configKey;
    /** 配置值 */
    private String configValue;
    /** 配置说明 */
    private String description;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;
}
