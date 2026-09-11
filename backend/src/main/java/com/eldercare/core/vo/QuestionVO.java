package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 问卷题目 VO（options 已解析为字符串列表）
 */
@Data
public class QuestionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 题目 ID */
    private Long id;
    /** 题目内容 */
    private String content;
    /** 类型：SINGLE 单选/MULTIPLE 多选/TEXT 文本 */
    private String type;
    /** 选项列表（文本题可为空） */
    private List<String> options;
    private String scoreMode;
    private Integer maxScore;
    /** 排序值（越小越靠前） */
    private Integer sortOrder;

}
