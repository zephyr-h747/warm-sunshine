package com.eldercare.core.mapper;

import com.eldercare.core.entity.HealthGuidance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】健康指导 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface HealthGuidanceMapper {

    int insert(HealthGuidance guidance);

    /** 查询某用户当天（create_time >= 今天 0 点）指定类型的指导，用于当日推送去重 */
    List<HealthGuidance> selectTodayByUserAndType(@Param("userId") Long userId, @Param("type") String type);
}
