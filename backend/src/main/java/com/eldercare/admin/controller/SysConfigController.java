package com.eldercare.admin.controller;

import com.eldercare.admin.service.SysConfigService;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.ConfigUpdateRequest;
import com.eldercare.core.entity.SysConfig;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 【管理端】系统配置接口（/api/admin/config）
 */
@RestController
@RequestMapping("/api/admin/config")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    /** 所有配置项 */
    @GetMapping
    public Result<List<SysConfig>> list() {
        return Result.success(sysConfigService.list());
    }

    /** 获取配置 */
    @GetMapping("/{key}")
    public Result<SysConfig> get(@PathVariable String key) {
        return Result.success(sysConfigService.get(key));
    }

    /** 更新配置（刷新 Redis 缓存） */
    @PutMapping("/{key}")
    public Result<SysConfig> update(@PathVariable String key, @Valid @RequestBody ConfigUpdateRequest req) {
        return Result.success("配置已更新", sysConfigService.update(key, req.getValue()));
    }
}
