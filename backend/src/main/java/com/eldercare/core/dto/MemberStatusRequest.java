package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 会员启用/禁用请求
 */
@Data
public class MemberStatusRequest {

    /** 账号状态：ENABLED 启用/DISABLED 禁用 */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态取值不合法")
    private String status;


}
