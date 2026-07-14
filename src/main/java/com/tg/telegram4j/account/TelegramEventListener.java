package com.tg.telegram4j.account;

import com.tg.telegram4j.model.TelegramMessage;

/**
 * Telegram 账号事件监听回调接口。
 * 由 TelegramAccountManager 实现，在 TelegramAccount 创建时传入。
 */
public interface TelegramEventListener {

    /**
     * 账号接收到新消息时回调。
     *
     * @param account 接收到消息的账号对象
     * @param message 封装后的消息体
     */
    void onMessage(TelegramAccount account, TelegramMessage message);

    /**
     * 账号断开连接时回调（网络异常、封号、服务端断开等）。
     *
     * @param account 断开连接的账号对象
     * @param reason  断开原因描述
     */
    void onDisconnect(TelegramAccount account, Integer errorCode, String reason);

    void onLoginSuccess(TelegramAccount account);

    void onLoginFailed(TelegramAccount account, Integer errorCode, String errorMsg);
}
