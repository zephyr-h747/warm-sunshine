package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 报告下载链接 VO（短期签名）
 */
@Data
public class ReportUrlVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 带签名的下载链接 */
    private String url;
    /** 过期时间戳（毫秒） */
    private Long expireTime;


    public ReportUrlVO(String url, Long expireTime) {
        this.url = url;
        this.expireTime = expireTime;
    }

}
