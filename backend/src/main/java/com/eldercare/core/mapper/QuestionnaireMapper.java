package com.eldercare.core.mapper;

import com.eldercare.core.entity.Questionnaire;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】问卷 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface QuestionnaireMapper {

    /** 已发布问卷列表（分页由 PageHelper 处理） */
    List<Questionnaire> selectPublishedList();

    /** 管理端：全部问卷列表（分页由 PageHelper 处理） */
    List<Questionnaire> selectList();

    Questionnaire selectById(@Param("id") Long id);

    int insert(Questionnaire questionnaire);

    int update(Questionnaire questionnaire);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 逻辑删除 */
    int delete(@Param("id") Long id);
}
