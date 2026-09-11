package com.eldercare.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新个人信息请求
 */
@Data
public class UpdateProfileRequest {

    /** 真实姓名 */
    @Size(max = 50, message = "姓名最长 50 字")
    private String realName;

    /** 性别：MALE/FEMALE/OTHER */
    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "性别取值不合法")
    private String gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 身高（cm），用于计算 BMI */
    @DecimalMin(value = "100.0", message = "身高不能低于 100cm")
    @Digits(integer = 3, fraction = 1, message = "身高格式不正确")
    private BigDecimal height;

    /** 头像 URL */
    @Size(max = 500, message = "头像地址过长")
    private String avatar;

    /** 紧急联系人 */
    @Size(max = 20, message = "紧急联系人最长 20 字")
    private String emergencyContact;

}
