package com.eldercare.core.scheduler;

import com.eldercare.core.entity.SmsCode;
import com.eldercare.core.mapper.SmsCodeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 定时任务单元测试（@SpringBootTest，需本机 MySQL + Redis 可用）：
 * - 手动触发清理验证码任务，验证只删除过期记录
 * - 其余任务冒烟：单独执行不抛异常（任务间隔离）
 */
@SpringBootTest
class SchedulerTaskTest {

    @Autowired
    private SchedulerTask schedulerTask;

    @Autowired
    private SmsCodeMapper smsCodeMapper;

    @Test
    void cleanExpiredSmsCode_deletesOnlyExpired() {
        String expiredPhone = "13900000010";
        String validPhone = "13900000011";

        SmsCode expired = new SmsCode();
        expired.setPhone(expiredPhone);
        expired.setCode("111111");
        expired.setExpireTime(LocalDateTime.now().minusMinutes(1));
        expired.setUsed(0);
        smsCodeMapper.insert(expired);

        SmsCode valid = new SmsCode();
        valid.setPhone(validPhone);
        valid.setCode("222222");
        valid.setExpireTime(LocalDateTime.now().plusMinutes(30));
        valid.setUsed(0);
        smsCodeMapper.insert(valid);

        // 手动触发清理任务
        schedulerTask.cleanExpiredSmsCode();

        // 过期记录被物理删除，有效记录保留
        assertNull(smsCodeMapper.selectLatestByPhone(expiredPhone), "过期验证码应被删除");
        assertNotNull(smsCodeMapper.selectLatestByPhone(validPhone), "有效验证码应保留");
    }

    @Test
    void allTasks_runIndependentlyWithoutThrowing() {
        // 单个任务 try-catch 隔离：各自执行均不抛异常
        assertDoesNotThrow(() -> schedulerTask.cleanExpiredPoints());
        assertDoesNotThrow(() -> schedulerTask.cleanExpiredAiMessages());
        assertDoesNotThrow(() -> schedulerTask.archiveExpiredAppointments());
        assertDoesNotThrow(() -> schedulerTask.pushAppointmentReminders());
        assertDoesNotThrow(() -> schedulerTask.syncDailyHealthStat());
    }
}
