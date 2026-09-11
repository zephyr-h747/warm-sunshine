package com.eldercare.core.vo;

import com.eldercare.core.entity.HealthRecord;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端会员详情 VO（基本信息 + 近期健康记录 + 预约记录）
 */
@Data
public class MemberDetailVO implements Serializable {

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
    /** 账号状态：ENABLED/DISABLED */
    private String status;
    /** 注册时间 */
    private LocalDateTime createTime;
    /** 近期健康记录（最近 5 条） */
    private List<HealthRecord> recentHealthRecords;
    /** 预约记录 */
    private List<AppointmentVO> appointments;

}
