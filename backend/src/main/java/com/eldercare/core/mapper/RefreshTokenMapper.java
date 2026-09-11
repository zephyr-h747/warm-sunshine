package com.eldercare.core.mapper;

import com.eldercare.core.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】刷新令牌 Mapper（无逻辑删除字段，登出物理删除）
 */
@Mapper
public interface RefreshTokenMapper {

    int insert(RefreshToken refreshToken);

    RefreshToken selectByToken(@Param("token") String token);

    List<RefreshToken> selectByUserId(@Param("userId") Long userId);

    int deleteByToken(@Param("token") String token);

    int deleteByUserId(@Param("userId") Long userId);

    /** 清理过期记录（定时任务用） */
    int deleteExpired();
}
