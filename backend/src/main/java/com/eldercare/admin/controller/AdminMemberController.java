package com.eldercare.admin.controller;

import com.eldercare.admin.service.AdminMemberService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.MemberLevelRequest;
import com.eldercare.core.dto.MemberPointsRequest;
import com.eldercare.core.dto.MemberStatusRequest;
import com.eldercare.core.vo.MemberAdminVO;
import com.eldercare.core.vo.MemberDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【管理端】会员管理接口（/api/admin/members）
 */
@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    /** 会员列表（?phone=&realName=&status=&memberLevel=&pageNum=&pageSize=） */
    @GetMapping
    public Result<PageResult<MemberAdminVO>> list(@RequestParam(required = false) String phone,
                                                  @RequestParam(required = false) String realName,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String memberLevel,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminMemberService.list(phone, realName, status, memberLevel, pageNum, pageSize));
    }

    /** 会员详情 */
    @GetMapping("/{id}")
    public Result<MemberDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminMemberService.detail(id));
    }

    /** 启用/禁用 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody MemberStatusRequest req) {
        adminMemberService.updateStatus(id, req.getStatus());
        return Result.success("会员状态已更新", null);
    }

    /** 调整等级 */
    @PutMapping("/{id}/level")
    public Result<Void> updateLevel(@PathVariable Long id, @Valid @RequestBody MemberLevelRequest req) {
        adminMemberService.updateMemberLevel(id, req.getMemberLevel());
        return Result.success("会员等级已更新", null);
    }

    /** 调整积分 */
    @PutMapping("/{id}/points")
    public Result<Void> adjustPoints(@PathVariable Long id, @Valid @RequestBody MemberPointsRequest req) {
        adminMemberService.adjustPoints(id, req.getDelta(), req.getReason());
        return Result.success("会员积分已调整", null);
    }

    /** 重置密码 */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        adminMemberService.resetPassword(id);
        return Result.success("密码已重置为默认密码", null);
    }
}
