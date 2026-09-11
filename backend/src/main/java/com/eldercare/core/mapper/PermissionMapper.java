package com.eldercare.core.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PermissionMapper {
    List<ApiResource> selectApiResourcesByUserId(@Param("userId") Long userId);
    record ApiResource(String path, String method) { }
}
