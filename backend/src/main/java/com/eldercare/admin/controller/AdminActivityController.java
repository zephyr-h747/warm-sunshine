package com.eldercare.admin.controller;

import com.eldercare.admin.service.AdminActivityService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.ActivityRequest;
import com.eldercare.core.entity.CommunityActivity;
import com.eldercare.core.vo.ActivityRegistrationVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【管理端】活动管理接口（/api/admin/activity）
 */
@RestController
@RequestMapping("/api/admin/activity")
public class AdminActivityController {

    private final AdminActivityService adminActivityService;

    public AdminActivityController(AdminActivityService adminActivityService) {
        this.adminActivityService = adminActivityService;
    }

    /** 创建活动 */
    @PostMapping
    public Result<CommunityActivity> create(@Valid @RequestBody ActivityRequest req) {
        return Result.success(adminActivityService.createActivity(req));
    }

    /** 编辑活动 */
    @PutMapping("/{id}")
    public Result<CommunityActivity> update(@PathVariable Long id, @Valid @RequestBody ActivityRequest req) {
        return Result.success(adminActivityService.updateActivity(id, req));
    }

    /** 删除活动 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminActivityService.deleteActivity(id);
        return Result.success("活动已删除", null);
    }

    /** 活动列表（分页 + 状态筛选） */
    @GetMapping
    public Result<PageResult<CommunityActivity>> list(@RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminActivityService.listActivities(status, pageNum, pageSize));
    }

    /** 活动详情 */
    @GetMapping("/{id}")
    public Result<CommunityActivity> detail(@PathVariable Long id) {
        return Result.success(adminActivityService.getActivityDetail(id));
    }

    /** 报名列表（分页） */
    @GetMapping("/{id}/registrations")
    public Result<PageResult<ActivityRegistrationVO>> registrations(@PathVariable Long id,
                                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminActivityService.listRegistrations(id, pageNum, pageSize));
    }
}
