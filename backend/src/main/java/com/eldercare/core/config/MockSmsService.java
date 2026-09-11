package com.eldercare.core.config;

import com.eldercare.core.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Mock 短信服务：不实际发送，仅记录日志（开发环境默认）
 * <p>
 * 生产环境配置 eldercare.sms.enabled=true 并提供阿里云实现后替换。
 */
@Service
@ConditionalOnProperty(name = "eldercare.sms.enabled", havingValue = "false", matchIfMissing = true)
public class MockSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(MockSmsService.class);

    @Override
    public void sendVerifyCode(String phone, String code) {
        log.info("[Mock短信] 验证码发送 -> 手机号={}, 验证码={}, 5分钟内有效", phone, code);
    }

    @Override
    public void sendNotification(String phone, String content) {
        log.info("[Mock短信] 通知发送 -> 手机号={}, 内容={}", phone, content);
    }
}
