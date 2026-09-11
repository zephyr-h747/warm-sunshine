package com.eldercare.admin.service;

import com.eldercare.core.mapper.ActivityRegistrationMapper;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.AdminStatisticsVO;
import org.springframework.stereotype.Service;

/**
 * 【管理端】仪表盘统计服务
 */
@Service
public class DashboardService {

    private final UserMapper userMapper;
    private final AppointmentMapper appointmentMapper;
    private final ActivityRegistrationMapper registrationMapper;

    public DashboardService(UserMapper userMapper, AppointmentMapper appointmentMapper,
                            ActivityRegistrationMapper registrationMapper) {
        this.userMapper = userMapper;
        this.appointmentMapper = appointmentMapper;
        this.registrationMapper = registrationMapper;
    }

    /** 仪表盘统计数据：会员总数 / 今日新增会员 / 今日预约 / 今日活动报名 / 待确认预约 */
    public AdminStatisticsVO getStatistics() {
        AdminStatisticsVO vo = new AdminStatisticsVO();
        vo.setMemberCount(userMapper.countAll());
        vo.setTodayNewMember(userMapper.countTodayNew());
        vo.setTodayAppointment(appointmentMapper.countToday());
        vo.setTodayActivityRegistration(registrationMapper.countToday());
        vo.setPendingAppointment(appointmentMapper.countPending());
        return vo;
    }
}
