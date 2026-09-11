package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理端创建/编辑问卷请求
 */
@Data
public class QuestionnaireRequest {

    /** 问卷标题 */
    @NotBlank(message = "问卷标题不能为空")
    @Size(max = 200, message = "问卷标题过长")
    private String title;

    /** 问卷说明 */
    @Size(max = 1000, message = "问卷描述过长")
    private String description;
    private Integer totalScore;
    private Integer passScore;
    private String gradeRules;


}
