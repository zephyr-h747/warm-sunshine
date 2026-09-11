package com.eldercare.api.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.dto.ActivityListQuery;
import com.eldercare.core.entity.ActivityRegistration;
import com.eldercare.core.entity.CommunityActivity;
import com.eldercare.core.entity.Message;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.ActivityRegistrationMapper;
import com.eldercare.core.mapper.CommunityActivityMapper;
import com.eldercare.core.mapper.MessageMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.SmsService;
import com.eldercare.core.vo.ActivityVO;
import com.eldercare.core.vo.MyActivityVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 【用户端】社区活动服务：
 * - 报名：状态 + 时间校验 → 唯一索引防重复 → 原子加人数（并发安全）→ 插入报名
 * - 签到：校验已报名 + 进行中 + 未签到 → 更新状态 → 原子加积分
 */
@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private static final String STATUS_REGISTRATING = "REGISTRATING";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_NOT_CHECKED_IN = "NOT_CHECKED_IN";
    private static final String STATUS_CHECKED_IN = "CHECKED_IN";
    /** 签到奖励积分 */
    private static final int CHECK_IN_POINTS = 50;

    private final CommunityActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final UserMapper userMapper;
    private final PointService pointService;
    private final MessageMapper messageMapper;
    private final SmsService smsService;

    public ActivityService(CommunityActivityMapper activityMapper,
                           ActivityRegistrationMapper registrationMapper,
                           UserMapper userMapper,
                           MessageMapper messageMapper,
                           SmsService smsService, PointService pointService) {
        this.activityMapper = activityMapper;
        this.registrationMapper = registrationMapper;
        this.userMapper = userMapper;
        this.messageMapper = messageMapper;
        this.smsService = smsService;
        this.pointService = pointService;
    }

    /** 活动列表（分页 + 状态筛选，附带当前用户报名状态） */
    public PageResult<ActivityVO> listActivities(ActivityListQuery query, Long userId) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<CommunityActivity> list = activityMapper.selectList(query.getStatus());
        PageInfo<CommunityActivity> pageInfo = new PageInfo<>(list);
        Map<Long, ActivityRegistration> regMap = findRegistrations(userId);
        List<ActivityVO> vos = list.stream().map(a -> toActivityVO(a, regMap)).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    /** 活动详情（附带当前用户报名状态） */
    public ActivityVO getActivityDetail(Long id, Long userId) {
        CommunityActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new ResourceNotFoundException("活动不存在");
        }
        return toActivityVO(activity, findRegistrations(userId));
    }

    /**
     * 报名活动（并发安全）：
     * 校验状态 + 报名时间 → 查重（唯一索引兜底）→ 原子加人数（未满才加）→ 插入报名
     */
    @Transactional
    public void register(Long userId, Long activityId) {
        CommunityActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ResourceNotFoundException("活动不存在");
        }
        if (!STATUS_REGISTRATING.equals(activity.getStatus())) {
            throw new BusinessException(400, "活动不在报名阶段");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getRegistrationStart() != null && now.isBefore(activity.getRegistrationStart())) {
            throw new BusinessException(400, "报名尚未开始");
        }
        if (activity.getRegistrationEnd() != null && now.isAfter(activity.getRegistrationEnd())) {
            throw new BusinessException(400, "报名已截止");
        }
        // 预检查重（并发下唯一索引 uk_user_activity 兜底）
        if (registrationMapper.selectByUserAndActivity(userId, activityId) != null) {
            throw new BusinessException(409, "请勿重复报名");
        }
        // 原子加人数：current_participants < max_participants 才 +1
        int incremented = activityMapper.incrementParticipants(activityId);
        if (incremented == 0) {
            throw new BusinessException(409, "活动名额已满");
        }
        // 插入报名记录（唯一索引冲突 → 事务回滚，同时退还人数）
        ActivityRegistration registration = new ActivityRegistration();
        registration.setUserId(userId);
        registration.setActivityId(activityId);
        try {
            registrationMapper.insert(registration);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "请勿重复报名", e);
        }

        sendSmsQuietly(userId, "您已成功报名活动「" + activity.getTitle() + "」，请准时参加。");
        log.info("活动报名成功: userId={}, activityId={}", userId, activityId);
    }

    /**
     * 签到（并发安全）：
     * 校验已报名 + 活动进行中 + 未签到 → 更新签到状态 → 原子加积分 50
     */
    @Transactional
    public void checkIn(Long userId, Long activityId) {
        ActivityRegistration registration = registrationMapper.selectByUserAndActivity(userId, activityId);
        if (registration == null) {
            throw new BusinessException(400, "未报名该活动，无法签到");
        }
        CommunityActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ResourceNotFoundException("活动不存在");
        }
        if (!STATUS_IN_PROGRESS.equals(activity.getStatus())) {
            throw new BusinessException(400, "活动尚未开始或已结束");
        }
        if (STATUS_CHECKED_IN.equals(registration.getCheckInStatus())) {
            throw new BusinessException(400, "请勿重复签到");
        }
        registrationMapper.updateCheckInStatus(registration.getId(), STATUS_CHECKED_IN);
        pointService.grant(userId, CHECK_IN_POINTS, "ACTIVITY_CHECKIN", String.valueOf(activityId), "活动签到奖励");

        saveMessageQuietly(userId, "签到成功",
                "您已完成活动「" + activity.getTitle() + "」签到，" + CHECK_IN_POINTS + " 积分已到账。", "ACTIVITY");
        sendSmsQuietly(userId, "您已完成活动「" + activity.getTitle() + "」签到，奖励 " + CHECK_IN_POINTS + " 积分已到账。");
        log.info("活动签到成功: userId={}, activityId={}, +{}积分", userId, activityId, CHECK_IN_POINTS);
    }

    /** 我的活动（分页） */
    public PageResult<MyActivityVO> listMyActivities(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ActivityRegistration> list = registrationMapper.selectByUserId(userId);
        PageInfo<ActivityRegistration> pageInfo = new PageInfo<>(list);
        List<MyActivityVO> vos = list.stream().map(reg -> {
            CommunityActivity activity = activityMapper.selectById(reg.getActivityId());
            return toMyActivityVO(activity, reg);
        }).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    /** 签到状态：NOT_CHECKED_IN/CHECKED_IN（未报名返回未签到） */
    public String getCheckInStatus(Long userId, Long activityId) {
        ActivityRegistration registration = registrationMapper.selectByUserAndActivity(userId, activityId);
        return registration != null ? registration.getCheckInStatus() : STATUS_NOT_CHECKED_IN;
    }

    // ==================== 私有方法 ====================

    /** 查询当前用户的全部报名记录，按活动 ID 建立索引（含签到状态） */
    private Map<Long, ActivityRegistration> findRegistrations(Long userId) {
        if (userId == null) {
            return Map.of();
        }
        return registrationMapper.selectByUserId(userId).stream()
                .collect(Collectors.toMap(ActivityRegistration::getActivityId, r -> r, (a, b) -> a));
    }

    private ActivityVO toActivityVO(CommunityActivity activity, Map<Long, ActivityRegistration> regMap) {
        ActivityVO vo = new ActivityVO();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setCoverUrl(activity.getCoverUrl());
        vo.setContent(activity.getContent());
        vo.setRegistrationStart(activity.getRegistrationStart());
        vo.setRegistrationEnd(activity.getRegistrationEnd());
        vo.setActivityStart(activity.getActivityStart());
        vo.setActivityEnd(activity.getActivityEnd());
        vo.setMaxParticipants(activity.getMaxParticipants());
        vo.setCurrentParticipants(activity.getCurrentParticipants());
        int max = activity.getMaxParticipants() == null ? 0 : activity.getMaxParticipants();
        int current = activity.getCurrentParticipants() == null ? 0 : activity.getCurrentParticipants();
        vo.setRemaining(Math.max(0, max - current));
        vo.setStatus(activity.getStatus());
        // 当前用户的报名与签到状态
        ActivityRegistration reg = regMap.get(activity.getId());
        vo.setRegistered(reg != null);
        vo.setCheckInStatus(reg != null ? reg.getCheckInStatus() : null);
        return vo;
    }

    private MyActivityVO toMyActivityVO(CommunityActivity activity, ActivityRegistration registration) {
        MyActivityVO vo = new MyActivityVO();
        vo.setId(registration.getActivityId());
        vo.setTitle(activity != null ? activity.getTitle() : null);
        vo.setCoverUrl(activity != null ? activity.getCoverUrl() : null);
        vo.setActivityStart(activity != null ? activity.getActivityStart() : null);
        vo.setActivityEnd(activity != null ? activity.getActivityEnd() : null);
        vo.setStatus(activity != null ? activity.getStatus() : null);
        vo.setCheckInStatus(registration.getCheckInStatus());
        vo.setCreateTime(registration.getCreateTime());
        return vo;
    }

    private void saveMessageQuietly(Long userId, String title, String content, String type) {
        try {
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setType(type);
            messageMapper.insert(message);
        } catch (Exception e) {
            log.warn("消息保存失败: userId={}, err={}", userId, e.getMessage());
        }
    }

    private void sendSmsQuietly(Long userId, String content) {
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                smsService.sendNotification(user.getPhone(), content);
            }
        } catch (Exception e) {
            log.warn("短信发送失败: userId={}, err={}", userId, e.getMessage());
        }
    }
}
