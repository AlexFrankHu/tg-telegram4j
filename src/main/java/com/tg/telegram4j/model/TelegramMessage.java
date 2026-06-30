package com.tg.telegram4j.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * 接收到的 Telegram 消息封装类。
 */
@Data
@Builder
public class TelegramMessage {

    /**
     * 消息ID
     */
    private int messageId;

    /**
     * 聊天ID
     */
    private long chatId;

    /**
     * 聊天类型（PRIVATE/GROUP/SUPERGROUP/CHANNEL）
     */
    private String chatType;

    /**
     * 聊天名称
     */
    private String chatName;

    /**
     * 发送者ID
     */
    private long senderId;

    /**
     * 发送者类型（USER/CHAT/CHANNEL）
     */
    private String senderType;

    /**
     * 发送者用户名
     */
    private String senderUsername;

    /**
     * 发送者名称（firstName + lastName）
     */
    private String senderName;

    /**
     * 消息文本内容
     */
    private String text;

    /**
     * 消息类型（TEXT/MEDIA/OTHER）
     */
    private String messageType;

    /**
     * 是否是自己发送的消息
     */
    private boolean outgoing;

    /**
     * 消息接收/发生时间
     */
    private Instant timestamp;

    /**
     * 原始消息对象（如需进一步处理可使用）
     */
    private transient Object rawMessage;
}
