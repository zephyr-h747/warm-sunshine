package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 问卷详情 VO（含题目列表）
 */
@Data
public class QuestionnaireVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 问卷 ID */
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
    /** 题目列表（按 sort_order 升序） */
    private List<QuestionVO> questions;


}
