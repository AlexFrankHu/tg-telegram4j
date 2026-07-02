package com.tg.telegram4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天记录表实体类。
 */
@Data
@TableName("tg_chat_message")
public class TgChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属账号ID */
    private Integer tgAccountId;

    /** 对话用户的Telegram ID */
    private Long chatId;

    /** Telegram消息ID */
    private Long messageId;

    /** 发送者用户ID */
    private Long senderUserId;

    /** 发送者聊天ID */
    private Long senderChatId;

    /** 发送者名称 */
    private String senderName;

    /** 是否为账号发出的消息 */
    private Integer isOutgoing;

    /** 发送时间 */
    private LocalDateTime sendTime;

    /** 内容类型: text/photo/video/voice/document等 */
    private String contentType;

    /** 文字内容或媒体描述 */
    private String textContent;

    /** 媒体文件ID */
    private Long mediaFileId;

    /** 媒体文件大小 */
    private Long mediaFileSize;

    /** MIME类型 */
    private String mediaMimeType;

    /** 媒体文件名 */
    private String mediaFileName;

    /** 媒体时长 */
    private Integer mediaDuration;

    /** 媒体宽度 */
    private Integer mediaWidth;

    /** 媒体高度 */
    private Integer mediaHeight;

    /** 缩略图文件ID */
    private Long thumbnailFileId;

    /** 账号所属节点ID */
    private String nodeId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
