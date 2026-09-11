package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评测结果实体（对应 assessment_result 表）
 */
@Data
public class AssessmentResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 评测用户 ID */
    private Long userId;
    /** 评测问卷 ID（关联 questionnaire） */
    private Long questionnaireId;
    /** 答案快照 JSON */
    private String answers;
    /** AI 评分（百分制） */
    private Integer aiScore;
    /** AI 健康建议文本 */
    private String aiSuggestion;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;


}
