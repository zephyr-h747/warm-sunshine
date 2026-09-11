package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评测结果 VO
 */
@Data
public class AssessmentResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 评测结果 ID */
    private Long id;
    /** 评测问卷 ID */
    private Long questionnaireId;
    /** 问卷标题 */
    private String questionnaireTitle;
    /** AI 评分（百分制） */
    private Integer aiScore;
    /** AI 健康建议 */
    private String aiSuggestion;
    /** 提交时间 */
    private LocalDateTime createTime;


}
