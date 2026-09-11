package com.eldercare.core.mapper;

import com.eldercare.core.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】消息 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface MessageMapper {

    int insert(Message message);

    /** 按用户查询（type 为空查全部，分页由 PageHelper 处理） */
    List<Message> selectByUserId(@Param("userId") Long userId, @Param("type") String type);

    /** 管理端：全站消息列表（分页由 PageHelper 处理） */
    List<Message> selectAll(@Param("type") String type);

    Message selectById(@Param("id") Long id);

    int updateIsRead(@Param("id") Long id, @Param("isRead") Integer isRead);

    /** 未读数量 */
    long countUnread(@Param("userId") Long userId);
}
