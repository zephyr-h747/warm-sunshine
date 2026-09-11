package com.eldercare.admin.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.dto.ActivityRequest;
import com.eldercare.core.entity.ActivityRegistration;
import com.eldercare.core.entity.CommunityActivity;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.ActivityRegistrationMapper;
import com.eldercare.core.mapper.CommunityActivityMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.ActivityRegistrationVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 【管理端】活动管理服务：活动 CRUD + 报名列表
 */
@Service
public class AdminActivityService {

    private static final Logger log = LoggerFactory.getLogger(AdminActivityService.class);

    private final CommunityActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final UserMapper userMapper;

    public AdminActivityService(CommunityActivityMapper activityMapper,
                                ActivityRegistrationMapper registrationMapper,
                                UserMapper userMapper) {
        this.activityMapper = activityMapper;
        this.registrationMapper = registrationMapper;
        this.userMapper = userMapper;
    }

    /** 创建活动：未指定状态时默认 DRAFT（草稿） */
    public CommunityActivity createActivity(ActivityRequest req) {
        CommunityActivity activity = new CommunityActivity();
        applyActivity(activity, req);
        activity.setStatus(req.getStatus() == null ? "DRAFT" : req.getStatus());
        activityMapper.insert(activity);
        log.info("管理端创建活动: id={}, title={}", activity.getId(), activity.getTitle());
        return activityMapper.selectById(activity.getId());
    }

    /** 更新活动：先校验存在性，再覆盖可编辑字段 */
    public CommunityActivity updateActivity(Long id, ActivityRequest req) {
        CommunityActivity activity = requireActivity(id);
        applyActivity(activity, req);
        activityMapper.update(activity);
        return activityMapper.selectById(id);
    }

    /** 删除活动：活动不存在时抛异常 */
    public void deleteActivity(Long id) {
        requireActivity(id);
        activityMapper.delete(id);
        log.info("管理端删除活动: id={}", id);
    }

    /** 活动列表（分页 + 状态筛选） */
    public PageResult<CommunityActivity> listActivities(String status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<CommunityActivity> list = activityMapper.selectList(status);
        return PageResult.of(new PageInfo<>(list));
    }

    /** 活动详情：不存在时抛 404 */
    public CommunityActivity getActivityDetail(Long id) {
        return requireActivity(id);
    }

    /** 报名列表（分页，含用户信息 + 签到状态） */
    public PageResult<ActivityRegistrationVO> listRegistrations(Long activityId, int pageNum, int pageSize) {
        requireActivity(activityId);
        PageHelper.startPage(pageNum, pageSize);
        List<ActivityRegistration> list = registrationMapper.selectByActivityId(activityId);
        PageInfo<ActivityRegistration> pageInfo = new PageInfo<>(list);
        List<ActivityRegistrationVO> vos = list.stream().map(this::toRegistrationVO).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getPageSize(), pageInfo.getPages());
    }

    // ==================== 私有方法 ====================

    private CommunityActivity requireActivity(Long id) {
        CommunityActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new ResourceNotFoundException("活动不存在");
        }
        return activity;
    }

    private void applyActivity(CommunityActivity activity, ActivityRequest req) {
        activity.setTitle(req.getTitle());
        activity.setCoverUrl(req.getCoverUrl());
        activity.setContent(req.getContent());
        activity.setRegistrationStart(req.getRegistrationStart());
        activity.setRegistrationEnd(req.getRegistrationEnd());
        activity.setActivityStart(req.getActivityStart());
        activity.setActivityEnd(req.getActivityEnd());
        activity.setMaxParticipants(req.getMaxParticipants());
        if (req.getStatus() != null) {
            activity.setStatus(req.getStatus());
        }
    }

    private ActivityRegistrationVO toRegistrationVO(ActivityRegistration registration) {
        ActivityRegistrationVO vo = new ActivityRegistrationVO();
        vo.setId(registration.getId());
        vo.setUserId(registration.getUserId());
        User user = userMapper.selectById(registration.getUserId());
        vo.setPhone(user != null ? user.getPhone() : null);
        vo.setRealName(user != null ? user.getRealName() : null);
        vo.setCheckInStatus(registration.getCheckInStatus());
        vo.setCreateTime(registration.getCreateTime());
        return vo;
    }
}
