package com.eldercare.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 会员等级调整请求
 */
@Data
public class MemberLevelRequest {

    /** 会员等级：NORMAL/SILVER/GOLD/PLATINUM/DIAMOND */
    @NotBlank(message = "等级不能为空")
    @Pattern(regexp = "NORMAL|SILVER|GOLD|PLATINUM|DIAMOND", message = "会员等级不合法")
    private String memberLevel;


}
