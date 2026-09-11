package com.eldercare.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 会员积分调整请求
 */
@Data
public class MemberPointsRequest {

    /** 积分变化量（正为增加，负为扣减） */
    @NotNull(message = "积分变化量不能为空")
    private Integer delta;

    /** 调整原因 */
    private String reason;


}
