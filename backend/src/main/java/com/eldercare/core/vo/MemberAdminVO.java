package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端会员列表 VO
 */
@Data
public class MemberAdminVO implements Serializable {

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
    /** 会员等级：NORMAL/SILVER/GOLD/PLATINUM/DIAMOND */
    private String memberLevel;
    /** 当前积分 */
    private Integer points;
    /** ENABLED/DISABLED */
    private String status;
    /** 注册时间 */
    private LocalDateTime createTime;


}
