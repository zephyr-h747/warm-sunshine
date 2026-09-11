package com.eldercare.core.mapper;

import com.eldercare.core.entity.SmsCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 【双端共用】短信验证码 Mapper（used 字段等价逻辑删除，过期记录定时物理清理）
 */
@Mapper
public interface SmsCodeMapper {

    int insert(SmsCode smsCode);

    /** 查询该手机号最新一条未使用验证码 */
    SmsCode selectLatestByPhone(@Param("phone") String phone);

    /** 标记验证码已使用 */
    int updateUsed(@Param("id") Long id);

    /** 物理删除过期记录（定时任务用） */
    int deleteExpired();
}
