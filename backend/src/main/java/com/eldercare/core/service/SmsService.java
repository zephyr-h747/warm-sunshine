package com.eldercare.core.service;

/**
 * 短信服务接口（阿里云短信实现可后续替换，开发期用 MockSmsService 打日志）
 */
public interface SmsService {

    /**
     * 发送验证码短信
     *
     * @param phone 手机号
     * @param code  验证码
     */
    void sendVerifyCode(String phone, String code);

    /**
     * 发送通知短信（预约提醒、活动提醒、健康提醒等）
     *
     * @param phone   手机号
     * @param content 通知内容
     */
    void sendNotification(String phone, String content);
}
