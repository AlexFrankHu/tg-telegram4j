package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账号配置表(兼容旧系统)实体类。
 */
@Data
@TableName("tg_account_config")
public class TgAccountConfig {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** TG用户ID */
    private Long tgUserId;

    /** 昵称 */
    private String nickname;

    /** 用户名 */
    private String username;

    /** 自定义用户名 */
    private String customUsername;

    /** 通知标志 */
    private Integer noticeFlag;

    /** 手机号 */
    private String phoneNum;

    /** 登录状态 */
    private String loginStatus;

    /** 手机号 */
    private String phone;

    /** API ID */
    private Integer apiId;

    /** API Hash */
    private String apiHash;

    /** 设备型号 */
    private String deviceModel;

    /** 系统版本 */
    private String systemVersion;

    /** APP版本 */
    private String appVersion;

    /** 语言代码 */
    private String langCode;

    /** 系统语言代码 */
    private String systemLangCode;

    /** 二步验证密码 */
    private String twoFaPassword;

    /** 最后在线时间 */
    private LocalDateTime lastOnlineTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
