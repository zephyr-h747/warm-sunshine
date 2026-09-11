package com.eldercare.core.mapper;

import com.eldercare.core.entity.HealthRecord;
import com.eldercare.core.vo.DailyHealthStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 【双端共用】健康记录 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface HealthRecordMapper {

    int insert(HealthRecord record);

    HealthRecord selectById(@Param("id") Long id);

    /** 按用户查询（分页由 PageHelper 处理） */
    List<HealthRecord> selectByUserId(@Param("userId") Long userId);

    /** 最近 6 个月记录（趋势分析用，按时间升序） */
    List<HealthRecord> selectRecent6Months(@Param("userId") Long userId);

    /** 定时任务：统计时间区间内的健康录入汇总（记录数 + 各指标均值） */
    DailyHealthStatVO selectDailySummary(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
