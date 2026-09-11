package com.eldercare.api.controller;

import com.eldercare.api.service.ProfileService;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.ChangePasswordRequest;
import com.eldercare.core.dto.UpdateProfileRequest;
import com.eldercare.core.util.SecurityUtils;
import com.eldercare.core.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【用户端】个人中心接口（/api/member/profile）
 */
@RestController
@RequestMapping("/api/member/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** 获取个人信息 */
    @GetMapping
    public Result<UserProfileVO> get() {
        return Result.success(profileService.getProfile(SecurityUtils.getCurrentUserId()));
    }

    /** 更新个人信息 */
    @PutMapping
    public Result<UserProfileVO> update(@Valid @RequestBody UpdateProfileRequest req) {
        return Result.success(profileService.updateProfile(SecurityUtils.getCurrentUserId(), req));
    }

    /** 修改密码（成功后强制下线所有设备） */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        profileService.changePassword(SecurityUtils.getCurrentUserId(), req);
        return Result.success("密码修改成功，请重新登录", null);
    }
}
