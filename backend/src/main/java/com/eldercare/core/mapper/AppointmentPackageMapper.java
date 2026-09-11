package com.eldercare.core.mapper;

import com.eldercare.core.entity.AppointmentPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【双端共用】体检套餐 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface AppointmentPackageMapper {

    /** 会员端：已启用套餐列表 */
    List<AppointmentPackage> selectList();

    /** 管理端：全部套餐列表（分页由 PageHelper 处理） */
    List<AppointmentPackage> selectAll();

    AppointmentPackage selectById(@Param("id") Long id);

    int insert(AppointmentPackage pkg);

    int update(AppointmentPackage pkg);

    /** 逻辑删除 */
    int delete(@Param("id") Long id);
}
