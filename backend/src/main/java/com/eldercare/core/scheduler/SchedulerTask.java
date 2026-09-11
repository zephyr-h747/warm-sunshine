package com.eldercare.core.scheduler;

import com.eldercare.core.mapper.AiConversationMessageMapper;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.HealthRecordMapper;
import com.eldercare.core.mapper.SmsCodeMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.mapper.ActivityRegistrationMapper;
import com.eldercare.api.service.PointService;
import com.eldercare.core.vo.ActivityReminderVO;
import com.eldercare.core.service.SmsService;
import com.eldercare.core.vo.AppointmentReminderVO;
import com.eldercare.core.vo.DailyHealthStatVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：
 * 1. 清理过期积分（注册超 1 年用户积分置 0）  每天 02:00
 * 2. 清理过期短信验证码（物理删除）           每 10 分钟
 * 3. 清理过期 AI 对话消息（逻辑删除 6 月前）  每天 03:00
 * 4. 归档过期历史预约（逻辑删除 2 年前已完成）每天 04:00
 * 5. 预约提醒推送（明日待完成预约短信）       每天 09:00
 * 6. 同步健康数据每日统计（汇总昨日录入）     每天 00:30
 * <p>
 * 每个任务 try-catch 隔离：单个任务失败不影响其他任务；记录开始/结束 INFO 日志，失败记 ERROR 日志。
 */
@Component
public class SchedulerTask {

    private static final Logger log = LoggerFactory.getLogger(SchedulerTask.class);

    private final UserMapper userMapper;
    private final SmsCodeMapper smsCodeMapper;
    private final AiConversationMessageMapper aiConversationMessageMapper;
    private final AppointmentMapper appointmentMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final SmsService smsService;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final PointService pointService;

    public SchedulerTask(UserMapper userMapper, SmsCodeMapper smsCodeMapper,
                         AiConversationMessageMapper aiConversationMessageMapper,
                         AppointmentMapper appointmentMapper, HealthRecordMapper healthRecordMapper,
                         SmsService smsService, ActivityRegistrationMapper activityRegistrationMapper,
                         PointService pointService) {
        this.userMapper = userMapper;
        this.smsCodeMapper = smsCodeMapper;
        this.aiConversationMessageMapper = aiConversationMessageMapper;
        this.appointmentMapper = appointmentMapper;
        this.healthRecordMapper = healthRecordMapper;
        this.smsService = smsService;
        this.activityRegistrationMapper = activityRegistrationMapper;
        this.pointService = pointService;
    }

    /** 每天凌晨 2 点：清理过期积分（无积分流水表，按注册时间 +1 年判断） */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredPoints() {
        String task = "清理过期积分";
        long start = System.currentTimeMillis();
        log.info("【定时任务】{} 开始", task);
        try {
            int affected = pointService.expireExpired();
            log.info("【定时任务】{} 完成: 过期积分批次 {} 个", task, affected);
        } catch (Exception e) {
            log.error("【定时任务】{} 失败: {}", task, e.getMessage(), e);
        } finally {
            log.info("【定时任务】{} 结束, 耗时 {}ms", task, System.currentTimeMillis() - start);
        }
    }

    /** 每天上午 8 点：提醒明日已报名活动的会员 */
    @Scheduled(cron = "0 0 8 * * ?")
    public void pushActivityReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<ActivityReminderVO> list = activityRegistrationMapper.selectTomorrowReminders(tomorrow.atStartOfDay(), tomorrow.plusDays(1).atStartOfDay());
        for (ActivityReminderVO item : list) {
            try { smsService.sendNotification(item.getPhone(), "温馨提示：您报名的活动「" + item.getTitle() + "」将于明日开始，请准时参加。"); }
            catch (Exception e) { log.warn("活动提醒发送失败: userId={}, activityId={}", item.getUserId(), item.getActivityId()); }
        }
        log.info("活动提醒完成: {} 条", list.size());
    }

    /** 每 10 分钟：物理删除过期短信验证码 */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void cleanExpiredSmsCode() {
        String task = "清理过期短信验证码";
        long start = System.currentTimeMillis();
        log.info("【定时任务】{} 开始", task);
        try {
            int affected = smsCodeMapper.deleteExpired();
            log.info("【定时任务】{} 完成: 删除 {} 条过期验证码", task, affected);
        } catch (Exception e) {
            log.error("【定时任务】{} 失败: {}", task, e.getMessage(), e);
        } finally {
            log.info("【定时任务】{} 结束, 耗时 {}ms", task, System.currentTimeMillis() - start);
        }
    }

    /** 每天凌晨 3 点：逻辑删除 6 个月前的 AI 对话消息 */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredAiMessages() {
        String task = "清理过期 AI 对话消息";
        long start = System.currentTimeMillis();
        log.info("【定时任务】{} 开始", task);
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMonths(6);
            int affected = aiConversationMessageMapper.logicDeleteExpired(cutoff);
            log.info("【定时任务】{} 完成: 逻辑删除 {} 条", task, affected);
        } catch (Exception e) {
            log.error("【定时任务】{} 失败: {}", task, e.getMessage(), e);
        } finally {
            log.info("【定时任务】{} 结束, 耗时 {}ms", task, System.currentTimeMillis() - start);
        }
    }

    /** 每天凌晨 4 点：逻辑删除 2 年前已完成的历史预约 */
    @Scheduled(cron = "0 0 4 * * ?")
    public void archiveExpiredAppointments() {
        String task = "归档过期历史预约";
        long start = System.currentTimeMillis();
        log.info("【定时任务】{} 开始", task);
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusYears(2);
            int affected = appointmentMapper.archiveExpired(cutoff);
            log.info("【定时任务】{} 完成: 归档 {} 条", task, affected);
        } catch (Exception e) {
            log.error("【定时任务】{} 失败: {}", task, e.getMessage(), e);
        } finally {
            log.info("【定时任务】{} 结束, 耗时 {}ms", task, System.currentTimeMillis() - start);
        }
    }

    /** 每天上午 9 点：查明日待完成预约（PENDING/CONFIRMED）发短信提醒 */
    @Scheduled(cron = "0 0 9 * * ?")
    public void pushAppointmentReminders() {
        String task = "预约提醒推送";
        long start = System.currentTimeMillis();
        log.info("【定时任务】{} 开始", task);
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<AppointmentReminderVO> list = appointmentMapper.selectPendingForDate(tomorrow);
            int sent = 0;
            for (AppointmentReminderVO item : list) {
                try {
                    smsService.sendNotification(item.getPhone(),
                            "温馨提示：您预约的「" + item.getPackageName() + "」体检将于明日 "
                                    + item.getAppointDate() + " " + item.getTimeRange() + " 进行，请准时参加。");
                    sent++;
                } catch (Exception e) {
                    log.warn("预约提醒短信发送失败: appointmentId={}, err={}", item.getAppointmentId(), e.getMessage());
                }
            }
            log.info("【定时任务】{} 完成: 明日待提醒 {} 条, 发送成功 {} 条", task, list.size(), sent);
        } catch (Exception e) {
            log.error("【定时任务】{} 失败: {}", task, e.getMessage(), e);
        } finally {
            log.info("【定时任务】{} 结束, 耗时 {}ms", task, System.currentTimeMillis() - start);
        }
    }

    /** 每天 0:30：汇总昨日健康录入数据（写入日志） */
    @Scheduled(cron = "0 30 0 * * ?")
    public void syncDailyHealthStat() {
        String task = "同步健康数据每日统计";
        long start = System.currentTimeMillis();
        log.info("【定时任务】{} 开始", task);
        try {
            LocalDateTime today = LocalDate.now().atStartOfDay();
            LocalDateTime yesterdayStart = today.minusDays(1);
            DailyHealthStatVO stat = healthRecordMapper.selectDailySummary(yesterdayStart, today);
            long count = stat == null || stat.getRecordCount() == null ? 0 : stat.getRecordCount();
            log.info("【定时任务】{} 完成: 昨日录入 {} 条, 平均收缩压 {}, 舒张压 {}, 血糖 {}, 心率 {}",
                    task, count,
                    stat == null ? null : stat.getAvgSystolic(),
                    stat == null ? null : stat.getAvgDiastolic(),
                    stat == null ? null : stat.getAvgBloodSugar(),
                    stat == null ? null : stat.getAvgHeartRate());
        } catch (Exception e) {
            log.error("【定时任务】{} 失败: {}", task, e.getMessage(), e);
        } finally {
            log.info("【定时任务】{} 结束, 耗时 {}ms", task, System.currentTimeMillis() - start);
        }
    }
}
