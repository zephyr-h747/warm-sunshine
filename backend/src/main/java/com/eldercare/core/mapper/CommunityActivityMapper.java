package com.eldercare.core.mapper;

import com.eldercare.core.entity.CommunityActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】社区活动 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface CommunityActivityMapper {

    /** 按状态筛选（status 为空查全部，分页由 PageHelper 处理），未结束的在前 */
    List<CommunityActivity> selectList(@Param("status") String status);

    CommunityActivity selectById(@Param("id") Long id);

    /** 原子加人数：仅当未满员时 +1，返回影响行数 */
    int incrementParticipants(@Param("id") Long id);

    /** 原子减人数：大于 0 才 -1，返回影响行数 */
    int decrementParticipants(@Param("id") Long id);

    /** 管理端：创建/编辑/删除 */
    int insert(CommunityActivity activity);

    int update(CommunityActivity activity);

    int delete(@Param("id") Long id);
}
