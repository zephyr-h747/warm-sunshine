package com.eldercare.api.controller;

import com.eldercare.api.service.HealthRecordService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.HealthRecordRequest;
import com.eldercare.core.entity.HealthRecord;
import com.eldercare.core.util.SecurityUtils;
import com.eldercare.core.vo.HealthTrendVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 【用户端】健康记录接口（/api/member/health）
 */
@RestController
@RequestMapping("/api/member/health")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    /** 录入健康记录（自动计算 BMI 并推送超标提醒） */
    @PostMapping
    public Result<HealthRecord> create(@Valid @RequestBody HealthRecordRequest req) {
        return Result.success(healthRecordService.create(SecurityUtils.getCurrentUserId(), req));
    }

    /** 历史记录（分页） */
    @GetMapping("/list")
    public Result<PageResult<HealthRecord>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(healthRecordService.list(SecurityUtils.getCurrentUserId(), pageNum, pageSize));
    }

    /** 近 6 个月趋势 */
    @GetMapping("/trend")
    public Result<List<HealthTrendVO>> trend() {
        return Result.success(healthRecordService.trend(SecurityUtils.getCurrentUserId()));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public Result<HealthRecord> detail(@PathVariable Long id) {
        return Result.success(healthRecordService.detail(SecurityUtils.getCurrentUserId(), id));
    }
}
