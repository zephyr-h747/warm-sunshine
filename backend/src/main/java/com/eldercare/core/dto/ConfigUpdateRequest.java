package com.eldercare.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 系统配置更新请求
 */
@Data
public class ConfigUpdateRequest {

    /** 新的配置值 */
    @NotNull(message = "配置值不能为空")
    private String value;

}
