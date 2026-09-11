package com.eldercare.core.mapper;

import com.eldercare.core.entity.PointTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PointTransactionMapper {
    int insert(PointTransaction transaction);
    List<PointTransaction> selectByUserId(@Param("userId") Long userId, @Param("type") String type);
    List<PointTransaction> selectExpirable(@Param("now") LocalDateTime now);
    int reduceRemain(@Param("id") Long id, @Param("amount") int amount);
    int expire(@Param("id") Long id);
    List<PointTransaction> selectActiveConsumptions(@Param("userId") Long userId, @Param("sourceType") String sourceType, @Param("sourceId") String sourceId);
    int restoreRemain(@Param("id") Long id, @Param("amount") int amount);
    int logicDelete(@Param("id") Long id);
}
