package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自动回复日志表实体类。
 */
@Data
@TableName("tg_auto_reply_log")
public class TgAutoReplyLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账号手机号 */
    private String accountPhone;

    /** 账号昵称/TG ID */
    private String accountNickname;

    /** 好友TG用户ID */
    private Long friendUserId;

    /** 好友昵称/TG ID */
    private String friendNickname;

    /** 好友手机号 */
    private String friendPhone;

    /** 触发类型: incoming/polling */
    private String triggerType;

    /** 请求state值(0-8) */
    private Integer state;

    /** 请求参数(JSON) */
    private String requestParams;

    /** 聊天上下文 */
    private String chatContext;

    /** 获取到的自动回复内容 */
    private String replyContent;

    /** 发送结果: success/failed/no_reply/api_error */
    private String sendResult;

    /** 错误原因 */
    private String errorReason;

    /** 账号所属节点ID */
    private String nodeId;

    /** 记录时间 */
    private LocalDateTime createTime;
}
