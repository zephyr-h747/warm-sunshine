package com.eldercare.core.mapper;

import com.eldercare.core.entity.AssessmentResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】评测结果 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface AssessmentResultMapper {

    int insert(AssessmentResult result);

    /** 按用户查询（分页由 PageHelper 处理） */
    List<AssessmentResult> selectByUserId(@Param("userId") Long userId);

    AssessmentResult selectById(@Param("id") Long id);
}
