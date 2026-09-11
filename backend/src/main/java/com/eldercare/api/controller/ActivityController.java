package com.eldercare.api.controller;

import com.eldercare.api.service.ActivityService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.ActivityListQuery;
import com.eldercare.core.util.SecurityUtils;
import com.eldercare.core.vo.ActivityVO;
import com.eldercare.core.vo.MyActivityVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【用户端】社区活动接口（/api/member/activity）
 */
@RestController
@RequestMapping("/api/member/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /** 活动列表（?status=&pageNum=&pageSize=，附带当前用户报名状态） */
    @GetMapping("/list")
    public Result<PageResult<ActivityVO>> list(ActivityListQuery query) {
        return Result.success(activityService.listActivities(query, SecurityUtils.getCurrentUserId()));
    }

    /** 活动详情（附带当前用户报名状态） */
    @GetMapping("/{id}")
    public Result<ActivityVO> detail(@PathVariable Long id) {
        return Result.success(activityService.getActivityDetail(id, SecurityUtils.getCurrentUserId()));
    }

    /** 报名 */
    @PostMapping("/{id}/register")
    public Result<Void> register(@PathVariable Long id) {
        activityService.register(SecurityUtils.getCurrentUserId(), id);
        return Result.success("报名成功", null);
    }

    /** 签到 */
    @PostMapping("/{id}/checkin")
    public Result<Void> checkIn(@PathVariable Long id) {
        activityService.checkIn(SecurityUtils.getCurrentUserId(), id);
        return Result.success("签到成功", null);
    }

    /** 我的活动 */
    @GetMapping("/mine")
    public Result<PageResult<MyActivityVO>> mine(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(activityService.listMyActivities(SecurityUtils.getCurrentUserId(), pageNum, pageSize));
    }

    /** 签到状态 */
    @GetMapping("/{id}/checkin-status")
    public Result<String> checkInStatus(@PathVariable Long id) {
        return Result.success(activityService.getCheckInStatus(SecurityUtils.getCurrentUserId(), id));
    }
}
