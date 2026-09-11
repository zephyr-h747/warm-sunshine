package com.eldercare.core.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录返回 VO
 */
@Data
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Access Token（短时效，请求需携带） */
    private String accessToken;
    /** Refresh Token（长时效，用于刷新 Access Token） */
    private String refreshToken;
    /** 用户 ID */
    private Long userId;
    /** 手机号 */
    private String phone;
    /** 真实姓名 */
    private String realName;
    /** 角色：MEMBER 会员/ADMIN 管理员 */
    private String role;
    /** 会员等级：NORMAL/SILVER/GOLD/PLATINUM/DIAMOND */
    private String memberLevel;
    /** 当前积分 */
    private Integer points;
    /** 头像 URL */
    private String avatar;

}
