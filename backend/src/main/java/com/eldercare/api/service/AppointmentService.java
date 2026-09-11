package com.eldercare.api.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.dto.CreateAppointmentRequest;
import com.eldercare.core.entity.Appointment;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.AccessDeniedException;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.AppointmentPackageMapper;
import com.eldercare.core.mapper.AppointmentSlotMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.service.SmsService;
import com.eldercare.core.vo.AppointmentVO;
import com.eldercare.core.vo.PackageVO;
import com.eldercare.core.vo.ReportUrlVO;
import com.eldercare.core.vo.SlotVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/**
 * 【用户端】体检预约服务：
 * - 预约采用「原子扣积分 + 原子加名额」保证并发安全，任一失败事务回滚
 * - 取消预约退还积分、释放名额
 * - 报告下载使用 5 分钟短期签名 URL（下载接口不直链磁盘，通过签名代理）
 */
@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELED = "CANCELED";

    /** 报告下载链接有效期 5 分钟 */
    private static final long REPORT_URL_TTL_MS = 5 * 60 * 1000L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AppointmentPackageMapper appointmentPackageMapper;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final AppointmentMapper appointmentMapper;
    private final UserMapper userMapper;
    private final SmsService smsService;
    private final PointService pointService;

    @Value("${eldercare.upload.path:./data/upload}")
    private String uploadPath;

    @Value("${jwt.secret:}")
    private String reportSecret;

    public AppointmentService(AppointmentPackageMapper appointmentPackageMapper,
                              AppointmentSlotMapper appointmentSlotMapper,
                              AppointmentMapper appointmentMapper,
                              UserMapper userMapper,
                              SmsService smsService, PointService pointService) {
        this.appointmentPackageMapper = appointmentPackageMapper;
        this.appointmentSlotMapper = appointmentSlotMapper;
        this.appointmentMapper = appointmentMapper;
        this.userMapper = userMapper;
        this.smsService = smsService;
        this.pointService = pointService;
    }

    /** 套餐列表（分页） */
    public PageResult<PackageVO> listPackages(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AppointmentPackage> list = appointmentPackageMapper.selectList();
        PageInfo<AppointmentPackage> pageInfo = new PageInfo<>(list);
        List<PackageVO> vos = list.stream().map(this::toPackageVO).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    /** 套餐详情 */
    public PackageVO getPackageDetail(Long id) {
        AppointmentPackage pkg = appointmentPackageMapper.selectById(id);
        if (pkg == null) {
            throw new ResourceNotFoundException("套餐不存在");
        }
        return toPackageVO(pkg);
    }

    /** 指定套餐 + 日期的可用时段 */
    public List<SlotVO> listSlots(Long packageId, LocalDate date) {
        AppointmentPackage pkg = appointmentPackageMapper.selectById(packageId);
        if (pkg == null || !"ENABLED".equals(pkg.getStatus())) {
            throw new ResourceNotFoundException("套餐不存在或不可预约");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException(400, "不能查看过去的日期");
        }
        List<AppointmentSlot> slots = appointmentSlotMapper.selectByPackageAndDate(packageId, date);
        return slots.stream().map(this::toSlotVO).toList();
    }

    /**
     * 创建预约（并发安全）：
     * 1. 原子扣积分（points>=price 才扣）
     * 2. 原子加名额（current_count<max_count 才加），失败回滚积分
     * 3. 插入预约（PENDING）
     */
    @Transactional
    public AppointmentVO createAppointment(Long userId, CreateAppointmentRequest req) {
        AppointmentPackage pkg = appointmentPackageMapper.selectById(req.getPackageId());
        if (pkg == null || !"ENABLED".equals(pkg.getStatus())) {
            throw new BusinessException(400, "套餐不可预约");
        }
        AppointmentSlot slot = appointmentSlotMapper.selectById(req.getSlotId());
        if (slot == null || !slot.getPackageId().equals(req.getPackageId())) {
            throw new BusinessException(400, "时段无效");
        }
        if ("CLOSED".equals(slot.getStatus())) {
            throw new BusinessException(400, "该时段已关闭");
        }

        // 1. 原子加名额（并发核心：current_count < max_count 才 +1）
        int incremented = appointmentSlotMapper.incrementCurrentCount(slot.getId());
        if (incremented == 0) {
            // 事务回滚，退还积分
            throw new BusinessException(409, "名额已满");
        }
        // 2. 插入预约；主键用于关联每个积分消费批次
        Appointment appointment = new Appointment();
        appointment.setUserId(userId);
        appointment.setSlotId(slot.getId());
        appointment.setPackageId(pkg.getId());
        appointment.setStatus(STATUS_PENDING);
        appointmentMapper.insert(appointment);
        // 3. 按 FIFO 扣积分并生成每个批次的消费流水，任一失败整体回滚
        try { pointService.consume(userId, pkg.getPrice(), "APPOINTMENT", String.valueOf(appointment.getId()), "预约体检套餐消费"); }
        catch (IllegalStateException e) { throw new BusinessException(409, "积分不足"); }

        // 4. 发送预约成功短信（容错，不影响主流程）
        sendSmsQuietly(userId, "您已成功预约「" + pkg.getName() + "」体检套餐，"
                + slot.getAppointDate() + " " + slot.getTimeRange() + "，请准时参加。");
        log.info("预约成功: userId={}, appointmentId={}, package={}", userId, appointment.getId(), pkg.getName());
        return toAppointmentVO(appointment, pkg, slot);
    }

    /**
     * 取消预约（并发安全）：
     * 校验归属 + 状态可取消 → 退积分 → 减名额 → 更新状态
     */
    @Transactional
    public void cancelAppointment(Long userId, Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new AccessDeniedException("无权操作他人的预约");
        }
        if (!STATUS_PENDING.equals(appointment.getStatus()) && !STATUS_CONFIRMED.equals(appointment.getStatus())) {
            throw new BusinessException(400, "当前状态不可取消");
        }
        AppointmentPackage pkg = appointmentPackageMapper.selectById(appointment.getPackageId());
        if (pkg != null) {
            try { pointService.refundAppointment(userId, appointmentId); }
            catch (IllegalStateException e) { throw new BusinessException(409, "预约积分流水异常，无法退款"); }
        }
        appointmentSlotMapper.decrementCurrentCount(appointment.getSlotId());
        appointmentMapper.updateStatus(appointmentId, STATUS_CANCELED);

        sendSmsQuietly(userId, pkg != null ? "您已取消「" + pkg.getName() + "」体检预约，积分已退还。" : "您已取消体检预约，积分已退还。");
        log.info("预约已取消: userId={}, appointmentId={}", userId, appointmentId);
    }

    /** 我的预约（分页） */
    public PageResult<AppointmentVO> listMyAppointments(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Appointment> list = appointmentMapper.selectByUserId(userId);
        PageInfo<Appointment> pageInfo = new PageInfo<>(list);
        List<AppointmentVO> vos = list.stream().map(a -> {
            AppointmentPackage pkg = appointmentPackageMapper.selectById(a.getPackageId());
            AppointmentSlot slot = appointmentSlotMapper.selectById(a.getSlotId());
            return toAppointmentVO(a, pkg, slot);
        }).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getPages());
    }

    /** 获取报告下载链接（5 分钟短期签名） */
    public ReportUrlVO getReportUrl(Long userId, Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("预约不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new AccessDeniedException("无权查看他人的体检报告");
        }
        if (!StringUtils.hasText(appointment.getReportUrl())) {
            throw new ResourceNotFoundException("报告尚未生成");
        }
        long expire = System.currentTimeMillis() + REPORT_URL_TTL_MS;
        String token = signReportToken(appointmentId, userId, expire);
        return new ReportUrlVO("/api/member/appointment/report/download?token=" + token, expire);
    }

    /** 报告下载（签名校验 + 过期校验 + 文件代理） */
    public ResponseEntity<byte[]> downloadReport(String token) {
        long[] parsed = verifyReportToken(token);
        Appointment appointment = appointmentMapper.selectById(parsed[0]);
        if (appointment == null || !StringUtils.hasText(appointment.getReportUrl())) {
            throw new ResourceNotFoundException("报告不存在或已被移除");
        }
        Path root = Path.of(uploadPath).toAbsolutePath().normalize();
        Path file = root.resolve(appointment.getReportUrl()).normalize();
        if (!file.startsWith(root) || !Files.exists(file)) {
            throw new ResourceNotFoundException("报告文件不存在");
        }
        try {
            byte[] data = Files.readAllBytes(file);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"report-" + appointment.getId() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (IOException e) {
            throw new BusinessException(500, "报告读取失败");
        }
    }

    // ==================== 私有方法 ====================

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

    private PackageVO toPackageVO(AppointmentPackage pkg) {
        PackageVO vo = new PackageVO();
        vo.setId(pkg.getId());
        vo.setName(pkg.getName());
        vo.setCoverUrl(pkg.getCoverUrl());
        vo.setDescription(pkg.getDescription());
        vo.setPrice(pkg.getPrice());
        vo.setSuitablePeople(pkg.getSuitablePeople());
        vo.setItems(parseItems(pkg.getItems()));
        vo.setStatus(pkg.getStatus());
        return vo;
    }

    private SlotVO toSlotVO(AppointmentSlot slot) {
        SlotVO vo = new SlotVO();
        vo.setId(slot.getId());
        vo.setPackageId(slot.getPackageId());
        vo.setAppointDate(slot.getAppointDate());
        vo.setTimeRange(slot.getTimeRange());
        vo.setMaxCount(slot.getMaxCount());
        vo.setCurrentCount(slot.getCurrentCount());
        int max = slot.getMaxCount() == null ? 0 : slot.getMaxCount();
        int current = slot.getCurrentCount() == null ? 0 : slot.getCurrentCount();
        vo.setRemaining(Math.max(0, max - current));
        vo.setStatus(slot.getStatus());
        return vo;
    }

    private AppointmentVO toAppointmentVO(Appointment a, AppointmentPackage pkg, AppointmentSlot slot) {
        AppointmentVO vo = new AppointmentVO();
        vo.setId(a.getId());
        vo.setPackageId(a.getPackageId());
        vo.setPackageName(pkg != null ? pkg.getName() : null);
        vo.setPrice(pkg != null ? pkg.getPrice() : null);
        vo.setSlotId(a.getSlotId());
        vo.setAppointDate(slot != null ? slot.getAppointDate() : null);
        vo.setTimeRange(slot != null ? slot.getTimeRange() : null);
        vo.setStatus(a.getStatus());
        vo.setReportUrl(a.getReportUrl());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }

    private List<String> parseItems(String itemsJson) {
        if (!StringUtils.hasText(itemsJson)) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(itemsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    // ==================== 报告签名 ====================

    /** 生成 5 分钟签名 token：Base64Url(appointmentId:userId:expire:sig) */
    private String signReportToken(Long appointmentId, Long userId, long expire) {
        String payload = appointmentId + ":" + userId + ":" + expire;
        String sig = hmac(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + ":" + sig).getBytes(StandardCharsets.UTF_8));
    }

    /** 校验签名 token，返回 [appointmentId, userId]；无效或过期抛 400 */
    private long[] verifyReportToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(400, "下载链接无效");
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 4) {
                throw new BusinessException(400, "下载链接无效");
            }
            String payload = parts[0] + ":" + parts[1] + ":" + parts[2];
            if (!hmac(payload).equals(parts[3])) {
                throw new BusinessException(400, "下载链接无效");
            }
            long expire = Long.parseLong(parts[2]);
            if (System.currentTimeMillis() > expire) {
                throw new BusinessException(400, "下载链接已过期，请重新获取");
            }
            return new long[]{Long.parseLong(parts[0]), Long.parseLong(parts[1])};
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "下载链接无效");
        }
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(reportSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("报告签名失败", e);
        }
    }
}
