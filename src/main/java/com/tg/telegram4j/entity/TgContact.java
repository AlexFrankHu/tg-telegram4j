package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友/联系人表实体类。
 */
@Data
@TableName("tg_contact")
public class TgContact {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属账号ID */
    private Integer tgAccountId;

    /** 好友的Telegram用户ID */
    private Long userId;

    /** 名 */
    private String firstName;

    /** 姓 */
    private String lastName;

    /** 昵称 */
    private String nickname;

    /** 用户名 */
    private String username;

    /** 手机号 */
    private String phoneNumber;

    /** 是否互为好友 */
    private Integer isMutual;

    /** 是否机器人 */
    private Integer isBot;

    /** 是否Premium用户 */
    private Integer isPremium;

    /** 是否认证 */
    private Integer isVerified;

    /** 用户类型: regular/bot/deleted */
    private String userType;

    /** 限制原因 */
    private String restrictionReason;

    /** 个人简介 */
    private String bio;

    /** 小头像文件ID */
    private Long photoSmallFileId;

    /** 大头像文件ID */
    private Long photoBigFileId;

    /** 最后在线时间 */
    private LocalDateTime lastOnlineTime;

    /** 最后发送时间(账号→好友) */
    private LocalDateTime lastSendTime;

    /** 最后接收时间(好友→账号) */
    private LocalDateTime lastReceiveTime;

    /** 是否开启自动回复 */
    private Integer autoReply;

    /** 来源: import/natural */
    private String source;

    /** 消息总数 */
    private Integer totalMsgCount;

    /** 账号发送数 */
    private Integer accountSentCount;

    /** 好友发送数 */
    private Integer friendSentCount;

    /** 账号所属节点ID */
    private String nodeId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
