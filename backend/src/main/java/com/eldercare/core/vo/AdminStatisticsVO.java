package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理端仪表盘统计 VO
 */
@Data
public class AdminStatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会员总数 */
    private long memberCount;
    /** 今日新增会员 */
    private long todayNewMember;
    /** 今日预约数 */
    private long todayAppointment;
    /** 今日活动报名数 */
    private long todayActivityRegistration;
    /** 待完成预约数 */
    private long pendingAppointment;


}
