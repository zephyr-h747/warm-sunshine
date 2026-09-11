package com.eldercare.admin.controller;

import com.eldercare.api.service.HealthRecordService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.entity.HealthRecord;
import com.eldercare.core.mapper.HealthRecordMapper;
import com.eldercare.core.vo.HealthTrendVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** 管理端会员健康档案与趋势查询。 */
@RestController
@RequestMapping("/api/admin/health-record")
public class AdminHealthRecordController {
    private final HealthRecordMapper healthRecordMapper;
    private final HealthRecordService healthRecordService;

    public AdminHealthRecordController(HealthRecordMapper healthRecordMapper, HealthRecordService healthRecordService) {
        this.healthRecordMapper = healthRecordMapper;
        this.healthRecordService = healthRecordService;
    }

    @GetMapping("/{memberId}")
    public Result<PageResult<HealthRecord>> list(@PathVariable Long memberId,
            @RequestParam(defaultValue="1") int pageNum,
            @RequestParam(defaultValue="10") int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<HealthRecord> records = healthRecordMapper.selectByUserId(memberId);
        return Result.success(PageResult.of(new PageInfo<>(records)));
    }

    @GetMapping("/{memberId}/trend")
    public Result<List<HealthTrendVO>> trend(@PathVariable Long memberId,
                                             @RequestParam(required=false) String indicator) {
        List<HealthTrendVO> result = healthRecordService.trend(memberId);
        if (indicator == null || indicator.isBlank()) return Result.success(result);
        return Result.success(result.stream().filter(v -> indicator.equalsIgnoreCase(v.getIndicator())).toList());
    }
}
