package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 管理端创建/编辑题目请求
 */

@Data
public class QuestionRequest {

    /** 所属问卷 ID */
    @NotNull(message = "问卷 ID 不能为空")
    private Long questionnaireId;

    /** 题目内容 */
    @NotBlank(message = "题目内容不能为空")
    private String content;

    /** 题目类型：SINGLE 单选/MULTIPLE 多选/TEXT 文本 */
    @NotBlank(message = "题目类型不能为空")
    @Pattern(regexp = "SINGLE|MULTIPLE|TEXT", message = "题目类型不合法")
    private String type;

    /** 选项（JSON 数组），文本题可为空 */
    private List<String> options;
    private String scoreMode;
    private Integer maxScore;

    /** 排序值（越小越靠前） */
    private Integer sortOrder;

}
