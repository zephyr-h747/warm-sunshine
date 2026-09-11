package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 刷新令牌返回 VO
 */
@Data
public class TokenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 新签发的 Access Token */
    private String accessToken;
    /** 令牌类型（Bearer） */
    private String tokenType;
    /** 有效期（秒） */
    private Integer expiresIn;


}
