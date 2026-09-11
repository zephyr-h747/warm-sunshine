package com.eldercare.core.mapper;

import com.eldercare.core.entity.AiConversationSession;
import com.eldercare.core.vo.SessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【用户端】AI 会话 Mapper
 */
@Mapper
public interface AiConversationSessionMapper {

    /** 新建会话（主键回填） */
    int insert(AiConversationSession session);

    /** 我的会话列表（含最后一条消息预览，按创建时间倒序，分页由 PageHelper 处理） */
    List<SessionVO> selectByUserId(@Param("userId") Long userId);

    /** 按主键查询会话（逻辑删除不可见） */
    AiConversationSession selectById(@Param("id") Long id);

    /** 逻辑删除会话，返回影响行数 */
    int deleteById(@Param("id") Long id);
}
