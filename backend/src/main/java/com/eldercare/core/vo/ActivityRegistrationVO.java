package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端活动报名列表 VO（含用户信息与签到状态）
 */
@Data
public class ActivityRegistrationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 报名记录 ID */
    private Long id;
    /** 报名用户 ID */
    private Long userId;
    /** 用户手机号 */
    private String phone;
    /** 用户真实姓名 */
    private String realName;
    /** NOT_CHECKED_IN/CHECKED_IN */
    private String checkInStatus;
    /** 报名时间 */
    private LocalDateTime createTime;

}
