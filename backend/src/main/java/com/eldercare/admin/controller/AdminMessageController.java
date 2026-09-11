package com.eldercare.admin.controller;

import com.eldercare.admin.service.AdminMessageService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.PushBatchMessageRequest;
import com.eldercare.core.dto.PushMessageRequest;
import com.eldercare.core.entity.Message;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【管理端】消息管理接口（/api/admin/message）
 */
@RestController
@RequestMapping("/api/admin/message")
public class AdminMessageController {

    private final AdminMessageService adminMessageService;

    public AdminMessageController(AdminMessageService adminMessageService) {
        this.adminMessageService = adminMessageService;
    }

    /** 全站消息列表（分页 + 类型筛选） */
    @GetMapping
    public Result<PageResult<Message>> list(@RequestParam(required = false) String type,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminMessageService.list(type, pageNum, pageSize));
    }

    /** 推送单条消息 */
    @PostMapping("/push")
    public Result<Void> push(@Valid @RequestBody PushMessageRequest req) {
        adminMessageService.push(req);
        return Result.success("消息已推送", null);
    }

    /** 批量推送 */
    @PostMapping("/push-batch")
    public Result<Integer> pushBatch(@Valid @RequestBody PushBatchMessageRequest req) {
        return Result.success("批量推送完成", adminMessageService.pushBatch(req));
    }
}
