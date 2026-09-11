package com.eldercare.core.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 创建预约请求
 */
public class CreateAppointmentRequest {

    /** 预约时段 ID（关联 appointment_slot） */
    @NotNull(message = "时段 ID 不能为空")
    private Long slotId;

    /** 体检套餐 ID（关联 appointment_package） */
    @NotNull(message = "套餐 ID 不能为空")
    private Long packageId;

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
}
