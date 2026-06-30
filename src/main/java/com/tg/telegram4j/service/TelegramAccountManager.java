package com.tg.telegram4j.service;

import com.tg.telegram4j.account.TelegramAccount;
import com.tg.telegram4j.config.TelegramProperties;
import com.tg.telegram4j.model.AccountLoginRequest;
import com.tg.telegram4j.model.SessionInfo;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TelegramAccountManager {

    private final TelegramProperties properties;
    private final Map<String, TelegramAccount> accounts = new ConcurrentHashMap<>();

    public TelegramAccountManager(TelegramProperties properties) {
        this.properties = properties;
    }

    /**
     * Login an account with the given request parameters.
     */
    public SessionInfo login(AccountLoginRequest request) {
        String name = request.getSessionName();
        if (accounts.containsKey(name)) {
            throw new IllegalStateException("Account '" + name + "' is already connected");
        }

        TelegramAccount account = new TelegramAccount(
                name,
                properties.getDataDir(),
                request.getProxy(),
                request.getDevice(),
                request.isAutoReadMessages(),
                request.getApiId(),
                request.getApiHash()
        );

        SessionInfo info = account.login(request.getSessionData());
        accounts.put(name, account);
        log.info("Account '{}' logged in, total active: {}", name, accounts.size());
        return info;
    }

    /**
     * Get an account by name.
     */
    public TelegramAccount getAccount(String sessionName) {
        return accounts.get(sessionName);
    }

    /**
     * List all active accounts.
     */
    public List<SessionInfo> listAccounts() {
        List<SessionInfo> list = new ArrayList<>();
        for (TelegramAccount account : accounts.values()) {
            SessionInfo info = account.getSessionInfo();
            if (info != null) {
                list.add(info);
            } else {
                list.add(SessionInfo.builder()
                        .sessionName(account.getSessionName())
                        .connected(account.isConnected())
                        .build());
            }
        }
        return list;
    }

    /**
     * Disconnect a specific account.
     */
    public boolean disconnect(String sessionName) {
        TelegramAccount account = accounts.remove(sessionName);
        if (account == null) {
            return false;
        }
        account.disconnect();
        log.info("Account '{}' disconnected, total active: {}", sessionName, accounts.size());
        return true;
    }

    /**
     * Disconnect all accounts.
     */
    @PreDestroy
    public void disconnectAll() {
        log.info("Disconnecting all accounts ({})...", accounts.size());
        for (String name : new ArrayList<>(accounts.keySet())) {
            disconnect(name);
        }
    }

    /**
     * Send a text message.
     */
    public Map<String, Object> sendTextMessage(String sessionName, String chatId, String text) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendTextMessage(chatId, text);
    }

    /**
     * Send an image message (by URL).
     */
    public Map<String, Object> sendImageMessage(String sessionName, String chatId, String imageUrl) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendImageMessage(chatId, imageUrl);
    }

    /**
     * Send an image message (by byte data).
     */
    public Map<String, Object> sendImageMessage(String sessionName, String chatId,
                                                 byte[] imageData, String fileName) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendImageMessage(chatId, imageData, fileName);
    }

    /**
     * Send a text + image (caption) message by URL.
     */
    public Map<String, Object> sendCaptionMessage(String sessionName, String chatId,
                                                   String caption, String imageUrl) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendCaptionMessage(chatId, caption, imageUrl);
    }

    /**
     * Send a text + image (caption) message by uploaded bytes.
     */
    public Map<String, Object> sendCaptionMessage(String sessionName, String chatId,
                                                   String caption, byte[] imageData, String fileName) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendCaptionMessage(chatId, caption, imageData, fileName);
    }

    private TelegramAccount getAccountOrThrow(String sessionName) {
        TelegramAccount account = accounts.get(sessionName);
        if (account == null) {
            throw new IllegalStateException("Account '" + sessionName + "' is not connected");
        }
        return account;
    }
}
