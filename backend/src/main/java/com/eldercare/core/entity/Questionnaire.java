package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问卷实体（对应 questionnaire 表）
 */
@Data
public class Questionnaire implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 问卷标题 */
    private String title;
    /** 问卷说明 */
    private String description;
    /** 状态：DRAFT 草稿/PUBLISHED 已发布 */
    private String status;
    private Integer totalScore;
    private Integer passScore;
    private String gradeRules;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;


}
