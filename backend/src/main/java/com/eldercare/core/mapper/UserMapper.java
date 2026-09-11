package com.eldercare.core.mapper;

import com.eldercare.core.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 【双端共用】用户 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface UserMapper {

    User selectByPhone(@Param("phone") String phone);

    User selectById(@Param("id") Long id);

    int insert(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
    /** 原子加减积分：points = points + delta */
    int updatePoints(@Param("id") Long id, @Param("delta") int delta);
    /** 原子扣积分：points = points - cost，仅当 points >= cost 时生效，返回影响行数 */
    int deductPoints(@Param("id") Long id, @Param("cost") int cost);
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateMemberLevel(@Param("id") Long id, @Param("memberLevel") String memberLevel);
    /** 更新个人信息 */
    int updateProfile(User user);

    /** 管理端：条件筛选分页（phone/realName 模糊，status/memberLevel 精确） */
    List<User> selectList(@Param("phone") String phone, @Param("realName") String realName,
                          @Param("status") String status, @Param("memberLevel") String memberLevel);

    /** 会员总数 */
    long countAll();

    /** 今日新增会员数 */
    long countTodayNew();

    /** 定时任务：注册超 cutoff 且积分>0 的用户数 */
    int countExpiredPointsUsers(@Param("cutoff") LocalDateTime cutoff);

    /** 定时任务：清空注册超 cutoff 用户的积分（置 0），返回影响行数 */
    int clearExpiredPoints(@Param("cutoff") LocalDateTime cutoff);
}
