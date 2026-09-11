package com.eldercare.api.controller;

import com.eldercare.api.service.PointService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.util.SecurityUtils;
import com.eldercare.core.vo.PointTransactionVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member/points")
public class PointController {
    private final PointService pointService;
    public PointController(PointService pointService) { this.pointService = pointService; }
    @GetMapping("/transactions")
    public Result<PageResult<PointTransactionVO>> transactions(@RequestParam(required=false) String type,
            @RequestParam(defaultValue="1") int pageNum, @RequestParam(defaultValue="10") int pageSize) {
        return Result.success(pointService.list(SecurityUtils.getCurrentUserId(), type, pageNum, pageSize));
    }
}
