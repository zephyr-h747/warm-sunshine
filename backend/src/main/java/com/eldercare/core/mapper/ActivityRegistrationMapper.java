package com.eldercare.core.mapper;

import com.eldercare.core.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.eldercare.core.vo.ActivityReminderVO;

/**
 * 【双端共用】活动报名 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface ActivityRegistrationMapper {

    int insert(ActivityRegistration registration);

    /** 查重：某用户是否已报名某活动 */
    ActivityRegistration selectByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /** 我的报名（分页由 PageHelper 处理） */
    List<ActivityRegistration> selectByUserId(@Param("userId") Long userId);

    ActivityRegistration selectById(@Param("id") Long id);

    int updateCheckInStatus(@Param("id") Long id, @Param("checkInStatus") String checkInStatus);

    /** 管理端：某活动报名列表（分页由 PageHelper 处理） */
    List<ActivityRegistration> selectByActivityId(@Param("activityId") Long activityId);

    /** 今日活动报名数 */
    long countToday();

    List<ActivityReminderVO> selectTomorrowReminders(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
