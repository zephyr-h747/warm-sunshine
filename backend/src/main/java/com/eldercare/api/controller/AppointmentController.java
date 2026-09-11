package com.eldercare.api.controller;

import com.eldercare.api.service.AppointmentService;
import com.eldercare.core.common.PageResult;
import com.eldercare.core.common.Result;
import com.eldercare.core.dto.CreateAppointmentRequest;
import com.eldercare.core.util.SecurityUtils;
import com.eldercare.core.vo.AppointmentVO;
import com.eldercare.core.vo.PackageVO;
import com.eldercare.core.vo.ReportUrlVO;
import com.eldercare.core.vo.SlotVO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 【用户端】体检预约接口（/api/member/appointment）
 */
@RestController
@RequestMapping("/api/member/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** 套餐列表 */
    @GetMapping("/packages")
    public Result<PageResult<PackageVO>> listPackages(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(appointmentService.listPackages(pageNum, pageSize));
    }

    /** 套餐详情 */
    @GetMapping("/packages/{id}")
    public Result<PackageVO> getPackage(@PathVariable Long id) {
        return Result.success(appointmentService.getPackageDetail(id));
    }

    /** 时段列表（?packageId=&date=yyyy-MM-dd） */
    @GetMapping("/slots")
    public Result<List<SlotVO>> listSlots(@RequestParam Long packageId,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(appointmentService.listSlots(packageId, date));
    }

    /** 创建预约 */
    @PostMapping
    public Result<AppointmentVO> create(@Valid @RequestBody CreateAppointmentRequest req) {
        return Result.success(appointmentService.createAppointment(SecurityUtils.getCurrentUserId(), req));
    }

    /** 取消预约 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(SecurityUtils.getCurrentUserId(), id);
        return Result.success("预约已取消", null);
    }

    /** 我的预约 */
    @GetMapping("/mine")
    public Result<PageResult<AppointmentVO>> mine(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(appointmentService.listMyAppointments(SecurityUtils.getCurrentUserId(), pageNum, pageSize));
    }

    /** 报告下载链接（带 5 分钟签名） */
    @GetMapping("/{id}/report")
    public Result<ReportUrlVO> report(@PathVariable Long id) {
        return Result.success(appointmentService.getReportUrl(SecurityUtils.getCurrentUserId(), id));
    }

    /** 报告下载（签名校验，permitAll） */
    @GetMapping("/report/download")
    public ResponseEntity<byte[]> download(@RequestParam String token) {
        return appointmentService.downloadReport(token);
    }
}
