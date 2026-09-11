package com.eldercare.admin.service;

import com.eldercare.core.common.PageResult;
import com.eldercare.core.dto.PackageRequest;
import com.eldercare.core.dto.SlotGenerateRequest;
import com.eldercare.core.entity.Appointment;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.entity.AppointmentSlot;
import com.eldercare.core.entity.User;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AppointmentMapper;
import com.eldercare.core.mapper.AppointmentPackageMapper;
import com.eldercare.core.mapper.AppointmentSlotMapper;
import com.eldercare.core.mapper.UserMapper;
import com.eldercare.core.vo.AdminAppointmentVO;
import com.eldercare.core.vo.PackageVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 【管理端】体检管理服务：
 * - 套餐 CRUD
 * - 批量生成时段
 * - 预约列表（按状态/日期筛选）
 * - 上传体检报告（仅 PDF，≤20MB）
 */
@Service
public class AdminAppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AdminAppointmentService.class);

    /** 报告文件大小上限 20MB */
    private static final long MAX_REPORT_SIZE = 20L * 1024 * 1024;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AppointmentPackageMapper packageMapper;
    private final AppointmentSlotMapper slotMapper;
    private final AppointmentMapper appointmentMapper;
    private final UserMapper userMapper;

    @Value("${eldercare.upload.path:./data/upload}")
    private String uploadPath;

    public AdminAppointmentService(AppointmentPackageMapper packageMapper, AppointmentSlotMapper slotMapper,
                                   AppointmentMapper appointmentMapper, UserMapper userMapper) {
        this.packageMapper = packageMapper;
        this.slotMapper = slotMapper;
        this.appointmentMapper = appointmentMapper;
        this.userMapper = userMapper;
    }

    // ==================== 套餐 CRUD ====================

    /** 创建套餐：未指定状态时默认 ENABLED（上架） */
    public AppointmentPackage createPackage(PackageRequest req) {
        AppointmentPackage pkg = new AppointmentPackage();
        applyPackage(pkg, req);
        pkg.setStatus(req.getStatus() == null ? "ENABLED" : req.getStatus());
        packageMapper.insert(pkg);
        log.info("管理端创建套餐: id={}, name={}", pkg.getId(), pkg.getName());
        return packageMapper.selectById(pkg.getId());
    }

    /** 更新套餐：先校验存在性，再覆盖可编辑字段 */
    public AppointmentPackage updatePackage(Long id, PackageRequest req) {
        AppointmentPackage existing = getPackage(id);
        applyPackage(existing, req);
        packageMapper.update(existing);
        return packageMapper.selectById(id);
    }

    /** 逻辑删除套餐 */
    public void deletePackage(Long id) {
        getPackage(id);
        packageMapper.delete(id);
        log.info("管理端删除套餐: id={}", id);
    }

    /** 套餐列表（分页，items 解析为列表，供前端直接渲染） */
    public PageResult<PackageVO> listPackages(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AppointmentPackage> list = packageMapper.selectAll();
        PageInfo<AppointmentPackage> pageInfo = new PageInfo<>(list);
        List<PackageVO> vos = list.stream().map(this::toPackageVO).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getPageSize(), pageInfo.getPages());
    }

    /** 套餐详情：不存在时抛 404 */
    public AppointmentPackage getPackage(Long id) {
        AppointmentPackage pkg = packageMapper.selectById(id);
        if (pkg == null) {
            throw new ResourceNotFoundException("套餐不存在");
        }
        return pkg;
    }

    // ==================== 批量生成时段 ====================

    /**
     * 批量生成时段：startDate 到 endDate 之间每天生成 timeRanges 中每个时段
     * 已存在的同套餐+日期+时段跳过，避免重复
     */
    public int generateSlots(SlotGenerateRequest req) {
        getPackage(req.getPackageId());
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new BusinessException(400, "结束日期不能早于开始日期");
        }
        // 收集已存在时段，用于去重
        List<AppointmentSlot> existing = slotMapper.selectByPackage(req.getPackageId());
        Set<String> existingKeys = existing.stream()
                .map(s -> s.getAppointDate() + "|" + s.getTimeRange())
                .collect(Collectors.toSet());

        List<AppointmentSlot> slots = new ArrayList<>();
        for (LocalDate date = req.getStartDate(); !date.isAfter(req.getEndDate()); date = date.plusDays(1)) {
            for (String timeRange : req.getTimeRanges()) {
                String key = date + "|" + timeRange;
                if (existingKeys.contains(key)) {
                    continue;
                }
                AppointmentSlot slot = new AppointmentSlot();
                slot.setPackageId(req.getPackageId());
                slot.setAppointDate(date);
                slot.setTimeRange(timeRange);
                slot.setMaxCount(req.getMaxCount());
                slot.setCurrentCount(0);
                slot.setStatus("AVAILABLE");
                slots.add(slot);
            }
        }
        if (slots.isEmpty()) {
            throw new BusinessException(400, "该套餐对应时段已全部生成，无需重复");
        }
        int count = slotMapper.batchInsert(slots);
        log.info("管理端批量生成时段: packageId={}, 数量={}", req.getPackageId(), count);
        return count;
    }

    // ==================== 预约列表 ====================

    /** 预约列表（按状态 + 日期区间筛选，分页） */
    public PageResult<AdminAppointmentVO> listAppointments(String status, LocalDate start, LocalDate end,
                                                           int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Appointment> list = appointmentMapper.selectList(status, start, end);
        PageInfo<Appointment> pageInfo = new PageInfo<>(list);
        List<AdminAppointmentVO> vos = list.stream().map(this::toAdminAppointmentVO).toList();
        return new PageResult<>(vos, pageInfo.getTotal(), pageInfo.getPageNum(),
                pageInfo.getPageSize(), pageInfo.getPages());
    }

    // ==================== 上传报告 ====================

    /** 管理端处理预约状态，限制合法状态流转。 */
    public void updateAppointmentStatus(Long appointmentId, String status) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) throw new ResourceNotFoundException("预约不存在");
        if (!Set.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELED").contains(status)) {
            throw new BusinessException(400, "预约状态无效");
        }
        if ("CANCELED".equals(appointment.getStatus()) || "COMPLETED".equals(appointment.getStatus())) {
            throw new BusinessException(400, "当前预约状态不可变更");
        }
        appointmentMapper.updateStatus(appointmentId, status);
    }

    /**
     * 上传体检报告（仅 PDF，≤20MB）：
     * 保存到 {uploadPath}/report/yyyyMM/{uuid}.pdf，更新预约 report_url 并置为已完成
     */
    public String uploadReport(Long appointmentId, MultipartFile file) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("预约不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }
        if (file.getSize() > MAX_REPORT_SIZE) {
            throw new BusinessException(400, "文件大小不能超过 20MB");
        }
        String originalName = file.getOriginalFilename();
        String ext = originalName == null ? "" : originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!"pdf".equals(ext)) {
            throw new BusinessException(400, "仅支持 PDF 文件");
        }

        try {
            String subDir = "report/" + DateTimeFormatter.ofPattern("yyyyMM").format(LocalDate.now());
            Path root = Path.of(uploadPath).toAbsolutePath().normalize();
            Path dir = root.resolve(subDir);
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID().toString().replace("-", "") + ".pdf";
            Path target = dir.resolve(storedName);
            file.transferTo(target.toAbsolutePath());
            String reportUrl = subDir + "/" + storedName;

            appointmentMapper.updateReportUrl(appointmentId, reportUrl);
            appointmentMapper.updateStatus(appointmentId, "COMPLETED");
            log.info("管理端上传报告: appointmentId={}, reportUrl={}, size={}B", appointmentId, reportUrl, file.getSize());
            return reportUrl;
        } catch (IOException e) {
            log.error("报告保存失败: appointmentId={}, err={}", appointmentId, e.getMessage(), e);
            throw new BusinessException(500, "报告保存失败");
        }
    }

    // ==================== 私有方法 ====================

    private void applyPackage(AppointmentPackage pkg, PackageRequest req) {
        pkg.setName(req.getName());
        pkg.setCoverUrl(req.getCoverUrl());
        pkg.setDescription(req.getDescription());
        pkg.setPrice(req.getPrice());
        pkg.setSuitablePeople(req.getSuitablePeople());
        pkg.setItems(toItemsJson(req.getItems()));
        if (req.getStatus() != null) {
            pkg.setStatus(req.getStatus());
        }
    }

    private String toItemsJson(List<String> items) {
        if (items == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(items);
        } catch (Exception e) {
            throw new BusinessException(400, "项目列表格式错误");
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

    private List<String> parseItems(String itemsJson) {
        if (itemsJson == null || itemsJson.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(itemsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private AdminAppointmentVO toAdminAppointmentVO(Appointment appointment) {
        AdminAppointmentVO vo = new AdminAppointmentVO();
        vo.setId(appointment.getId());
        vo.setUserId(appointment.getUserId());
        User user = userMapper.selectById(appointment.getUserId());
        vo.setUserPhone(user != null ? user.getPhone() : null);
        vo.setUserName(user != null ? user.getRealName() : null);
        AppointmentPackage pkg = packageMapper.selectById(appointment.getPackageId());
        vo.setPackageName(pkg != null ? pkg.getName() : null);
        vo.setPrice(pkg != null ? pkg.getPrice() : null);
        AppointmentSlot slot = slotMapper.selectById(appointment.getSlotId());
        vo.setAppointDate(slot != null ? slot.getAppointDate() : null);
        vo.setTimeRange(slot != null ? slot.getTimeRange() : null);
        vo.setStatus(appointment.getStatus());
        vo.setReportUrl(appointment.getReportUrl());
        vo.setCreateTime(appointment.getCreateTime());
        return vo;
    }
}
