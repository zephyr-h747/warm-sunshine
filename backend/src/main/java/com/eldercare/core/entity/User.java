package com.eldercare.core.entity;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体（对应 user 表）
 */
@Data
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 手机号（登录账号，唯一） */
    private String phone;
    /** 登录密码（BCrypt 加密存储） */
    private String password;
    /** 真实姓名 */
    private String realName;
    /** 性别 */
    private String gender;
    /** 出生日期 */
    private LocalDate birthDate;
    /** 身高（cm），用于计算 BMI */
    private BigDecimal height;
    /** 头像 URL */
    private String avatar;
    /** 紧急联系人 */
    private String emergencyContact;
    /** 会员等级：NORMAL/SILVER/GOLD/PLATINUM/DIAMOND */
    private String memberLevel;
    private Integer points;
    /** 状态：ENABLED/DISABLED */
    private String status;
    /** 角色：MEMBER/ADMIN */
    private String role;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 逻辑删除：0 未删除/1 已删除 */
    private Integer deleted;

}
