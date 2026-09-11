package com.eldercare.core.mapper;

import com.eldercare.core.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】问卷题目 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface QuestionMapper {

    /** 按问卷查询题目（按 sort_order 升序） */
    List<Question> selectByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    Question selectById(@Param("id") Long id);

    int insert(Question question);

    int update(Question question);

    /** 逻辑删除 */
    int delete(@Param("id") Long id);

    /** 逻辑删除某问卷下全部题目 */
    int deleteByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);
}
