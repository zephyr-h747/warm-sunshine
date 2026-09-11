package com.eldercare.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交评测请求
 */
@Data
public class AssessmentSubmitRequest {

    /** 评测问卷 ID */
    @NotNull(message = "问卷 ID 不能为空")
    private Long questionnaireId;

    /** 答案列表（每个题目一条） */
    @NotEmpty(message = "答案不能为空")
    @Valid
    private List<AnswerItem> answers;

    /**
     * 单个题目答案
     */
    @Data
    public static class AnswerItem {

        /** 题目 ID */
        @NotNull(message = "题目 ID 不能为空")
        private Long qid;

        /** 题目类型：SINGLE 单选/MULTIPLE 多选/TEXT 文本 */
        @NotNull(message = "题目类型不能为空")
        private String type;

        /** 答案值：SINGLE 为字符串，MULTIPLE 为字符串数组，TEXT 为文本 */
        @NotNull(message = "答案不能为空")
        private Object value;

    }
}
