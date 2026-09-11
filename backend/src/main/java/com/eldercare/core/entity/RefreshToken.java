package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 刷新令牌实体（对应 refresh_token 表，无逻辑删除字段）
 */
@Data
public class RefreshToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 所属用户 ID */
    private Long userId;
    /** 刷新令牌内容（JWT） */
    private String token;
    /** 过期时间 */
    private LocalDateTime expireTime;
    /** 创建时间 */
    private LocalDateTime createTime;

}
