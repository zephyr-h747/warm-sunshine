package com.eldercare.admin.controller;

import com.eldercare.admin.service.AdminAppointmentService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.PackageRequest;
import com.eldercare.core.dto.SlotGenerateRequest;
import com.eldercare.core.entity.AppointmentPackage;
import com.eldercare.core.vo.AdminAppointmentVO;
import com.eldercare.core.vo.PackageVO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 【管理端】体检管理接口（/api/admin/appointment）
 */
@RestController
@RequestMapping("/api/admin/appointment")
public class AdminAppointmentController {

    private final AdminAppointmentService adminAppointmentService;

    public AdminAppointmentController(AdminAppointmentService adminAppointmentService) {
        this.adminAppointmentService = adminAppointmentService;
    }

    /** 创建套餐 */
    @PostMapping("/packages")
    public Result<AppointmentPackage> createPackage(@Valid @RequestBody PackageRequest req) {
        return Result.success(adminAppointmentService.createPackage(req));
    }

    /** 编辑套餐 */
    @PutMapping("/packages/{id}")
    public Result<AppointmentPackage> updatePackage(@PathVariable Long id, @Valid @RequestBody PackageRequest req) {
        return Result.success(adminAppointmentService.updatePackage(id, req));
    }

    /** 删除套餐（逻辑删除） */
    @DeleteMapping("/packages/{id}")
    public Result<Void> deletePackage(@PathVariable Long id) {
        adminAppointmentService.deletePackage(id);
        return Result.success("套餐已删除", null);
    }

    /** 套餐列表（分页，items 已解析为列表） */
    @GetMapping("/packages")
    public Result<PageResult<PackageVO>> listPackages(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminAppointmentService.listPackages(pageNum, pageSize));
    }

    /** 批量生成时段 */
    @PostMapping("/slots/generate")
    public Result<Integer> generateSlots(@Valid @RequestBody SlotGenerateRequest req) {
        return Result.success("时段生成成功", adminAppointmentService.generateSlots(req));
    }

    /** 预约列表（?status=&start=&end=&pageNum=&pageSize=） */
    @GetMapping("/list")
    public Result<PageResult<AdminAppointmentVO>> listAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(adminAppointmentService.listAppointments(status, start, end, pageNum, pageSize));
    }

    /** 处理预约状态（PENDING/CONFIRMED/COMPLETED/CANCELED） */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminAppointmentService.updateAppointmentStatus(id, status);
        return Result.success("预约状态已更新", null);
    }

    /** 上传体检报告（multipart） */
    @PostMapping("/{id}/report")
    public Result<String> uploadReport(@PathVariable Long id,
                                       @RequestParam("file") MultipartFile file) {
        return Result.success("报告上传成功", adminAppointmentService.uploadReport(id, file));
    }
}
