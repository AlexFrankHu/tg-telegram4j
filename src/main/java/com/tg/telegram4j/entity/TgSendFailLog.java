package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发送失败日志表实体类。
 */
@Data
@TableName("tg_send_fail_log")
public class TgSendFailLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 账号手机号 */
    private String phone;

    /** 账号ID */
    private Integer tgAccountId;

    /** 好友user_id */
    private Long userId;

    /** 内容类型 */
    private String contentType;

    /** 发送内容 */
    private String content;

    /** 错误原因 */
    private String errorReason;

    /** 发送时间 */
    private LocalDateTime sendTime;

    /** 账号所属节点ID */
    private String nodeId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
