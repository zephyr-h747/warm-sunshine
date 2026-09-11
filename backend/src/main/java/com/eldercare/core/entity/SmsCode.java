package com.eldercare.core.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信验证码实体（对应 sms_code 表，used 字段等价逻辑删除）
 */
@Data
public class SmsCode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 接收验证码的手机号 */
    private String phone;
    /** 验证码内容 */
    private String code;
    /** 过期时间 */
    private LocalDateTime expireTime;
    /** 是否已使用：0 未使用/1 已使用 */
    private Integer used;
    /** 创建时间 */
    private LocalDateTime createTime;
}
