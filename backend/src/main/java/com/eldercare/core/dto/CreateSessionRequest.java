package com.eldercare.core.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建 AI 会话请求
 */
@Data
public class CreateSessionRequest {

    /** 会话名称（为空时默认“新对话”） */
    @Size(max = 100, message = "会话名称不能超过 100 字")
    private String sessionName;
}
