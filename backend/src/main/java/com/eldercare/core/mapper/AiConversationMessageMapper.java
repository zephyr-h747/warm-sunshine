package com.eldercare.core.mapper;

import com.eldercare.core.entity.AiConversationMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 【双端共用】AI 对话消息 Mapper（用户端对话接口 + 定时清理过期消息）
 */
@Mapper
public interface AiConversationMessageMapper {

    /** 新增对话消息（主键回填） */
    int insert(AiConversationMessage message);

    /** 会话消息历史（按时间升序，分页由 PageHelper 处理） */
    List<AiConversationMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    /** 最近 10 轮对话（最新 20 条消息，按时间升序返回，作为 AI 上下文） */
    List<AiConversationMessage> selectRecent10Messages(@Param("sessionId") Long sessionId);

    /** 逻辑删除某会话全部消息（删除会话时联动），返回影响行数 */
    int logicDeleteBySessionId(@Param("sessionId") Long sessionId);

    /** 定时任务：逻辑删除 cutoff 之前的历史对话消息，返回影响行数 */
    int logicDeleteExpired(@Param("cutoff") LocalDateTime cutoff);
}
