package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人中心 VO
 */
@Data
public class UserProfileVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long id;
    /** 手机号 */
    private String phone;
    /** 真实姓名 */
    private String realName;
    /** 性别 */
    private String gender;
    /** 出生日期 */
    private LocalDate birthDate;
    /** 身高（cm） */
    private BigDecimal height;
    /** 头像 URL */
    private String avatar;
    /** 紧急联系人 */
    private String emergencyContact;
    /** 会员等级：NORMAL/SILVER/GOLD/PLATINUM/DIAMOND */
    private String memberLevel;
    /** 当前积分 */
    private Integer points;
    /** 注册时间 */
    private LocalDateTime createTime;


}
