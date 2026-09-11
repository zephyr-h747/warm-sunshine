package com.eldercare.core.mapper;

import com.eldercare.core.entity.AppointmentSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 【双端共用】体检时段 Mapper（用户端 + 管理端共用）
 */
@Mapper
public interface AppointmentSlotMapper {

    List<AppointmentSlot> selectByPackageAndDate(@Param("packageId") Long packageId, @Param("date") LocalDate date);

    AppointmentSlot selectById(@Param("id") Long id);

    /** 原子加名额：current_count < max_count 才 +1，返回影响行数 */
    int incrementCurrentCount(@Param("id") Long id);

    /** 原子减名额：current_count > 0 才 -1，返回影响行数 */
    int decrementCurrentCount(@Param("id") Long id);

    /** 管理端：批量生成时段 */
    int batchInsert(List<AppointmentSlot> slots);

    /** 管理端：某套餐全部时段 */
    List<AppointmentSlot> selectByPackage(@Param("packageId") Long packageId);
}
