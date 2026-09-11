package com.eldercare.core.mapper;

import com.eldercare.core.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】系统配置 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface SysConfigMapper {

    List<SysConfig> selectList();

    SysConfig selectByKey(@Param("configKey") String configKey);

    int updateByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
