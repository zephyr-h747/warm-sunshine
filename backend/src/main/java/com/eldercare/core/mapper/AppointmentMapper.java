package com.eldercare.core.mapper;

import com.eldercare.core.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import com.eldercare.core.vo.AppointmentReminderVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 【双端共用】预约 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface AppointmentMapper {

    int insert(Appointment appointment);

    Appointment selectById(@Param("id") Long id);

    /** 按用户查询（分页由 PageHelper 处理） */
    List<Appointment> selectByUserId(@Param("userId") Long userId);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateReportUrl(@Param("id") Long id, @Param("reportUrl") String reportUrl);

    /** 管理端：按状态 + 日期区间筛选（分页由 PageHelper 处理） */
    List<Appointment> selectList(@Param("status") String status,
                                 @Param("start") LocalDate start, @Param("end") LocalDate end);

    /** 某日新增预约数 */
    long countToday();

    /** 待确认预约数（仅 PENDING，仪表盘"待确认预约"卡片） */
    long countPending();

    /** 定时任务：逻辑删除 cutoff 之前已完成的历史预约，返回影响行数 */
    int archiveExpired(@Param("cutoff") LocalDateTime cutoff);

    /** 定时任务：某日待完成预约（PENDING/CONFIRMED），联表带用户手机号与套餐名 */
    List<AppointmentReminderVO> selectPendingForDate(@Param("date") LocalDate date);
}
