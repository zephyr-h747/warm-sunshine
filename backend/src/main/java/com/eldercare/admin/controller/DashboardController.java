package com.eldercare.admin.controller;

import com.eldercare.admin.service.DashboardService;
import com.eldercare.core.common.Result;
import com.eldercare.core.vo.AdminStatisticsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【管理端】仪表盘接口（/api/admin/dashboard）
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** 统计数据 */
    @GetMapping
    public Result<AdminStatisticsVO> getStatistics() {
        return Result.success(dashboardService.getStatistics());
    }
}
