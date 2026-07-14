package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Telethon账号管理表实体类。
 */
@Data
@TableName("tg_telethon_account")
public class TgTelethonAccount {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 手机号 */
    private String phone;

    /** Telegram API ID */
    private Integer apiId;

    /** Telegram API Hash */
//    @JsonIgnore
    private String apiHash;

    /** Telegram用户ID */
    private Long tgUserId;

    /** 昵称(firstName + lastName) */
    private String nickname;

    /** 用户名 */
    private String username;

    /** 手机号归属国 */
    private String country;

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

    /** 导入批次号 */
    private String batchNo;

    /** 状态: online/offline/banned/restricted/failed/login1/login2 */
    private String status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 是否已删除 */
    private Integer isDeleted;

    /** 代理IP的ID */
    private Integer proxyIpId;

    /** 代理IP组号 */
    private String proxyGroupNo;

    /** 完整代理URL */
    private String proxyUrl;

    /** 代理协议(socks5/http) */
    private String proxyProtocol;

    /** 代理地址 */
    private String proxyHost;

    /** 代理端口 */
    private Integer proxyPort;

    /** 代理认证用户名 */
    private String proxyUsername;

    /** 代理认证密码 */
//    @JsonIgnore
    private String proxyPassword;

    /** 是否开启自动回复 */
    private Integer autoReply;

    /** 是否被限制 */
    private Integer isRestricted;

    /** 消息总数 */
    private Integer totalMsgCount;

    /** 发送总数 */
    private Integer sentMsgCount;

    /** 接收总数 */
    private Integer recvMsgCount;

    /** 账号所属节点ID */
    private String nodeId;

    /** 账号JSON文件内容 */
//    @JsonIgnore
    private String jsonContent;

    /** 账号session文件内容(二进制) */
    @JsonIgnore
    private byte[] sessionContent;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    private String errorMsg;
}
