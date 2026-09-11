package com.eldercare.admin.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.entity.Appointment;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.entity.HealthRecord;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.AppointmentPackageMapper;
import com.eldercare.core.mapper.AppointmentSlotMapper;
import com.eldercare.core.mapper.HealthRecordMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.AppointmentVO;
import com.eldercare.core.vo.MemberAdminVO;
import com.eldercare.core.vo.MemberDetailVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.eldercare.api.service.PointService;

/**
 * 【管理端】会员管理服务
 */
@Service
public class AdminMemberService {

    private static final Logger log = LoggerFactory.getLogger(AdminMemberService.class);

    /** 重置后的默认密码 */
    @org.springframework.beans.factory.annotation.Value("${ADMIN_RESET_PASSWORD}")
    private String defaultPassword;

    private static final String USER_BLACKLIST_PREFIX = "jwt:blacklist:user:";
    private static final long USER_BLACKLIST_TTL_SECONDS = 7 * 24 * 3600L;

    private final UserMapper userMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentPackageMapper appointmentPackageMapper;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final PointService pointService;

    public AdminMemberService(UserMapper userMapper, HealthRecordMapper healthRecordMapper,
                              AppointmentMapper appointmentMapper, AppointmentPackageMapper appointmentPackageMapper,
                              AppointmentSlotMapper appointmentSlotMapper, PasswordEncoder passwordEncoder,
                              StringRedisTemplate stringRedisTemplate, PointService pointService) {
        this.userMapper = userMapper;
        this.healthRecordMapper = healthRecordMapper;
        this.appointmentMapper = appointmentMapper;
        this.appointmentPackageMapper = appointmentPackageMapper;
        this.appointmentSlotMapper = appointmentSlotMapper;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
        this.pointService = pointService;
    }

    /** 会员列表（分页 + 条件筛选） */
    public PageResult<MemberAdminVO> list(String phone, String realName, String status, String memberLevel,
                                          int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectList(phone, realName, status, memberLevel);
        PageInfo<User> pageInfo = new PageInfo<>(list);
        List<MemberAdminVO> vos = list.stream().map(this::toMemberAdminVO).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getPageSize(), pageInfo.getPages());
    }

    /** 会员详情：基本信息 + 近期健康记录 + 预约记录 */
    public MemberDetailVO detail(Long id) {
        User user = getUser(id);
        MemberDetailVO vo = toMemberDetailVO(user);

        List<HealthRecord> records = healthRecordMapper.selectByUserId(id);
        vo.setRecentHealthRecords(records.size() > 5 ? records.subList(0, 5) : records);

        List<Appointment> appointments = appointmentMapper.selectByUserId(id);
        vo.setAppointments(appointments.stream().map(this::toAppointmentVO).toList());
        return vo;
    }

    /** 启用/禁用 */
    public void updateStatus(Long id, String status) {
        getUser(id);
        userMapper.updateStatus(id, status);
        // 禁用时强制下线该用户所有会话
        if ("DISABLED".equals(status)) {
            stringRedisTemplate.opsForValue().set(USER_BLACKLIST_PREFIX + id, "1",
                    USER_BLACKLIST_TTL_SECONDS, TimeUnit.SECONDS);
        }
        log.info("管理端调整会员状态: userId={}, status={}", id, status);
    }

    /** 调整会员等级 */
    public void updateMemberLevel(Long id, String memberLevel) {
        getUser(id);
        userMapper.updateMemberLevel(id, memberLevel);
        log.info("管理端调整会员等级: userId={}, level={}", id, memberLevel);
    }

    /** 调整积分（原子操作） */
    public void adjustPoints(Long id, int delta, String reason) {
        getUser(id);
        if (delta > 0) pointService.grant(id, delta, "ADMIN_ADJUST", String.valueOf(id), reason);
        else if (delta < 0) pointService.consume(id, -delta, "ADMIN_ADJUST", String.valueOf(id), reason);
        log.info("管理端调整积分: userId={}, delta={}, reason={}", id, delta, reason);
    }

    /** 重置密码为默认密码，并强制下线 */
    public void resetPassword(Long id) {
        getUser(id);
        userMapper.updatePassword(id, passwordEncoder.encode(defaultPassword));
        stringRedisTemplate.opsForValue().set(USER_BLACKLIST_PREFIX + id, "1",
                USER_BLACKLIST_TTL_SECONDS, TimeUnit.SECONDS);
        log.info("管理端重置会员密码: userId={}", id);
    }

    // ==================== 私有方法 ====================

    private User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ResourceNotFoundException("会员不存在");
        }
        return user;
    }

    private MemberAdminVO toMemberAdminVO(User user) {
        MemberAdminVO vo = new MemberAdminVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setGender(user.getGender());
        vo.setMemberLevel(user.getMemberLevel());
        vo.setPoints(user.getPoints());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    private MemberDetailVO toMemberDetailVO(User user) {
        MemberDetailVO vo = new MemberDetailVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setGender(user.getGender());
        vo.setBirthDate(user.getBirthDate());
        vo.setHeight(user.getHeight());
        vo.setAvatar(user.getAvatar());
        vo.setEmergencyContact(user.getEmergencyContact());
        vo.setMemberLevel(user.getMemberLevel());
        vo.setPoints(user.getPoints());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    private AppointmentVO toAppointmentVO(Appointment appointment) {
        AppointmentPackage pkg = appointmentPackageMapper.selectById(appointment.getPackageId());
        AppointmentSlot slot = appointmentSlotMapper.selectById(appointment.getSlotId());
        AppointmentVO vo = new AppointmentVO();
        vo.setId(appointment.getId());
        vo.setPackageId(appointment.getPackageId());
        vo.setPackageName(pkg != null ? pkg.getName() : null);
        vo.setPrice(pkg != null ? pkg.getPrice() : null);
        vo.setSlotId(appointment.getSlotId());
        vo.setAppointDate(slot != null ? slot.getAppointDate() : null);
        vo.setTimeRange(slot != null ? slot.getTimeRange() : null);
        vo.setStatus(appointment.getStatus());
        vo.setReportUrl(appointment.getReportUrl());
        vo.setCreateTime(appointment.getCreateTime());
        return vo;
    }
}

