package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问卷题目实体（对应 question 表）
 */
@Data
public class Question implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 所属问卷 ID（关联 questionnaire） */
    private Long questionnaireId;
    /** 题目内容 */
    private String content;
    /** 类型：SINGLE 单选/MULTIPLE 多选/TEXT 文本 */
    private String type;
    /** 选项 JSON 数组，如 ["A.选项1","B.选项2"] */
    private String options;
    private String scoreMode;
    private Integer maxScore;
    /** 排序值（越小越靠前） */
    private Integer sortOrder;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;


}
