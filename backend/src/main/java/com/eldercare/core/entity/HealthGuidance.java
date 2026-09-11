package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康指导实体（对应 health_guidance 表）
 */
@Data
public class HealthGuidance implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 指导所属用户 ID */
    private Long userId;
    /** 类型：DIET 饮食/EXERCISE 运动/DAILY 作息/DATA_SUMMARY 数据小结 */
    private String type;
    /** 指导内容（AI 生成文本） */
    private String content;
    /** 是否已读：0 未读/1 已读 */
    private Integer isRead;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;


}
