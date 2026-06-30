package com.tg.telegram4j.service;

import com.tg.telegram4j.account.TelegramAccount;
import com.tg.telegram4j.entity.TgTelethonAccount;
import com.tg.telegram4j.model.AccountLoginRequest;
import com.tg.telegram4j.model.DeviceInfo;
import com.tg.telegram4j.model.ProxyInfo;
import com.tg.telegram4j.model.SessionInfo;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TelegramAccountManager {

    @Value("${telegram.data-dir:./data}")
    private String dataDir;

    private final TgTelethonAccountService accountService;
    private final Map<String, TelegramAccount> accounts = new ConcurrentHashMap<>();

    public TelegramAccountManager(TgTelethonAccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 登录账号（通过请求参数）。
     */
    public SessionInfo login(AccountLoginRequest request) {
        String name = request.getSessionName();
        if (accounts.containsKey(name)) {
            throw new IllegalStateException("Account '" + name + "' is already connected");
        }

        TelegramAccount account = new TelegramAccount(
                name,
                dataDir,
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
     * 从数据库加载账号并登录。
     * 根据数据库记录的 session_content、代理信息、设备信息自动创建 TelegramAccount 并登录。
     */
    public SessionInfo loginFromDb(TgTelethonAccount dbAccount) {
        String name = dbAccount.getPhone();
        if (accounts.containsKey(name)) {
            throw new IllegalStateException("Account '" + name + "' is already connected");
        }

        if (dbAccount.getSessionContent() == null || dbAccount.getSessionContent().length == 0) {
            throw new IllegalArgumentException("Account '" + name + "' has no session_content in database");
        }

        // 从数据库记录构建代理信息
        ProxyInfo proxyInfo = null;
        if (dbAccount.getProxyHost() != null && !dbAccount.getProxyHost().isBlank()
                && dbAccount.getProxyPort() != null && dbAccount.getProxyPort() > 0) {
            proxyInfo = ProxyInfo.builder()
                    .host(dbAccount.getProxyHost())
                    .port(dbAccount.getProxyPort())
                    .username(dbAccount.getProxyUsername())
                    .password(dbAccount.getProxyPassword())
                    .build();
        }

        // 从数据库记录构建设备信息
        DeviceInfo deviceInfo = null;
        if (dbAccount.getDeviceModel() != null || dbAccount.getSystemVersion() != null
                || dbAccount.getAppVersion() != null) {
            deviceInfo = DeviceInfo.builder()
                    .deviceModel(dbAccount.getDeviceModel())
                    .systemVersion(dbAccount.getSystemVersion())
                    .appVersion(dbAccount.getAppVersion())
                    .langCode(dbAccount.getLangCode())
                    .systemLangCode(dbAccount.getSystemLangCode())
                    .build();
        }

        TelegramAccount account = new TelegramAccount(
                name,
                dataDir,
                proxyInfo,
                deviceInfo,
                false,
                dbAccount.getApiId(),
                dbAccount.getApiHash()
        );

        SessionInfo info = account.login(dbAccount.getSessionContent());
        accounts.put(name, account);

        // 登录成功后更新数据库中的账号信息
        try {
            String nickname = "";
            if (info.getFirstName() != null) {
                nickname = info.getFirstName();
            }
            if (info.getLastName() != null && !info.getLastName().isBlank()) {
                nickname = nickname + " " + info.getLastName();
            }
            accountService.updateAfterLogin(
                    dbAccount.getId(),
                    info.getUserId(),
                    nickname.trim(),
                    info.getUsername(),
                    info.getPhone()
            );
        } catch (Exception e) {
            log.warn("Failed to update DB after login for '{}': {}", name, e.getMessage());
        }

        log.info("Account '{}' logged in from DB, total active: {}", name, accounts.size());
        return info;
    }

    /**
     * 根据名称获取账号对象。
     */
    public TelegramAccount getAccount(String sessionName) {
        return accounts.get(sessionName);
    }

    /**
     * 获取所有在线账号列表。
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
     * 断开指定账号的连接。
     */
    public boolean disconnect(String sessionName) {
        TelegramAccount account = accounts.remove(sessionName);
        if (account == null) {
            return false;
        }
        account.disconnect();

        // 更新数据库状态为 offline
        try {
            TgTelethonAccount dbAccount = accountService.getByPhone(sessionName);
            if (dbAccount != null) {
                accountService.updateStatus(dbAccount.getId(), "offline");
            }
        } catch (Exception e) {
            log.warn("Failed to update DB status after disconnect for '{}': {}", sessionName, e.getMessage());
        }

        log.info("Account '{}' disconnected, total active: {}", sessionName, accounts.size());
        return true;
    }

    /**
     * 断开所有账号的连接（应用关闭时调用）。
     */
    @PreDestroy
    public void disconnectAll() {
        log.info("Disconnecting all accounts ({})...", accounts.size());
        for (String name : new ArrayList<>(accounts.keySet())) {
            disconnect(name);
        }
    }

    /**
     * 发送纯文本消息。
     */
    public Map<String, Object> sendTextMessage(String sessionName, String chatId, String text) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendTextMessage(chatId, text);
    }

    /**
     * 发送图片消息（通过URL）。
     */
    public Map<String, Object> sendImageMessage(String sessionName, String chatId, String imageUrl) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendImageMessage(chatId, imageUrl);
    }

    /**
     * 发送图片消息（通过字节数组）。
     */
    public Map<String, Object> sendImageMessage(String sessionName, String chatId,
                                                 byte[] imageData, String fileName) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendImageMessage(chatId, imageData, fileName);
    }

    /**
     * 发送 文本+图片(caption) 消息（通过URL）。
     */
    public Map<String, Object> sendCaptionMessage(String sessionName, String chatId,
                                                   String caption, String imageUrl) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendCaptionMessage(chatId, caption, imageUrl);
    }

    /**
     * 发送 文本+图片(caption) 消息（通过上传字节数组）。
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
