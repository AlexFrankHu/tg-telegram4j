package com.tg.telegram4j.controller;

import com.tg.telegram4j.entity.TgTelethonAccount;
import com.tg.telegram4j.model.*;
import com.tg.telegram4j.service.TelegramAccountManager;
import com.tg.telegram4j.service.TgTelethonAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final TelegramAccountManager accountManager;
    private final TgTelethonAccountService accountService;

    public AccountController(TelegramAccountManager accountManager,
                             TgTelethonAccountService accountService) {
        this.accountManager = accountManager;
        this.accountService = accountService;
    }

    /**
     * 登录账号（通过请求参数传入 session 字节数据）。
     */
    @PostMapping("/login")
    public ApiResponse<SessionInfo> login(@RequestBody AccountLoginRequest request) {
        try {
            SessionInfo info = accountManager.login(request);
            return ApiResponse.ok("Login successful", info);
        } catch (Exception e) {
            log.error("Login failed for '{}'", request.getSessionName(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 通过上传 .session 文件登录（multipart方式）。
     */
    @PostMapping("/login/upload")
    public ApiResponse<SessionInfo> loginByUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionName") String sessionName,
            @RequestParam(value = "autoReadMessages", required = false, defaultValue = "false") boolean autoRead,
            @RequestParam(value = "proxyHost", required = false) String proxyHost,
            @RequestParam(value = "proxyPort", required = false, defaultValue = "0") int proxyPort,
            @RequestParam(value = "proxyUsername", required = false) String proxyUsername,
            @RequestParam(value = "proxyPassword", required = false) String proxyPassword,
            @RequestParam(value = "deviceModel", required = false) String deviceModel,
            @RequestParam(value = "systemVersion", required = false) String systemVersion,
            @RequestParam(value = "appVersion", required = false) String appVersion) {
        try {
            AccountLoginRequest request = new AccountLoginRequest();
            request.setSessionName(sessionName);
            request.setSessionData(file.getBytes());
            request.setAutoReadMessages(autoRead);

            if (proxyHost != null && !proxyHost.isBlank()) {
                request.setProxy(ProxyInfo.builder()
                        .host(proxyHost).port(proxyPort)
                        .username(proxyUsername).password(proxyPassword)
                        .build());
            }
            if (deviceModel != null || systemVersion != null || appVersion != null) {
                request.setDevice(DeviceInfo.builder()
                        .deviceModel(deviceModel)
                        .systemVersion(systemVersion)
                        .appVersion(appVersion)
                        .build());
            }

            SessionInfo info = accountManager.login(request);
            return ApiResponse.ok("Login successful", info);
        } catch (Exception e) {
            log.error("Upload login failed for '{}'", sessionName, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 从数据库加载指定账号并登录。
     * 账号的 session_content、代理、设备信息都从 tg_telethon_account 表读取。
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/account/login/db/1
     * </pre>
     */
    @PostMapping("/login/db/{accountId}")
    public ApiResponse<SessionInfo> loginFromDb(@PathVariable Integer accountId) {
        try {
            TgTelethonAccount dbAccount = accountService.getById(accountId);
            if (dbAccount == null) {
                return ApiResponse.error("Account not found in database: id=" + accountId);
            }
            SessionInfo info = accountManager.loginFromDb(dbAccount);
            return ApiResponse.ok("Login successful", info);
        } catch (Exception e) {
            log.error("DB login failed for account id={}", accountId, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 从数据库加载指定手机号的账号并登录。
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/account/login/db/phone/84582563441
     * </pre>
     */
    @PostMapping("/login/db/phone/{phone}")
    public ApiResponse<SessionInfo> loginFromDbByPhone(@PathVariable String phone) {
        try {
            TgTelethonAccount dbAccount = accountService.getByPhone(phone);
            if (dbAccount == null) {
                return ApiResponse.error("Account not found in database: phone=" + phone);
            }
            SessionInfo info = accountManager.loginFromDb(dbAccount);
            return ApiResponse.ok("Login successful", info);
        } catch (Exception e) {
            log.error("DB login failed for phone={}", phone, e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量从数据库加载指定状态的账号并登录。
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/account/login/db/batch?status=offline
     * </pre>
     */
    @PostMapping("/login/db/batch")
    public ApiResponse<List<Map<String, Object>>> loginBatchFromDb(
            @RequestParam(value = "status", required = false, defaultValue = "offline") String status,
            @RequestParam(value = "nodeId", required = false) String nodeId) {
        try {
            List<TgTelethonAccount> dbAccounts;
            if (nodeId != null && !nodeId.isBlank()) {
                dbAccounts = accountService.listByNodeId(nodeId);
            } else {
                dbAccounts = accountService.listByStatus(status);
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (TgTelethonAccount dbAccount : dbAccounts) {
                Map<String, Object> result = new java.util.LinkedHashMap<>();
                result.put("phone", dbAccount.getPhone());
                result.put("id", dbAccount.getId());
                try {
                    if (dbAccount.getSessionContent() == null || dbAccount.getSessionContent().length == 0) {
                        result.put("success", false);
                        result.put("error", "No session_content");
                    } else {
                        SessionInfo info = accountManager.loginFromDb(dbAccount);
                        result.put("success", true);
                        result.put("userId", info.getUserId());
                        result.put("username", info.getUsername());
                    }
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("error", e.getMessage());
                }
                results.add(result);
            }
            return ApiResponse.ok("Batch login completed", results);
        } catch (Exception e) {
            log.error("Batch DB login failed", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询数据库中的所有账号记录。
     */
    @GetMapping("/db/list")
    public ApiResponse<List<TgTelethonAccount>> listDbAccounts(
            @RequestParam(value = "status", required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ApiResponse.ok(accountService.listByStatus(status));
        }
        return ApiResponse.ok(accountService.listAll());
    }

    /**
     * 获取所有在线账号列表。
     */
    @GetMapping("/list")
    public ApiResponse<List<SessionInfo>> listAccounts() {
        return ApiResponse.ok(accountManager.listAccounts());
    }

    /**
     * 获取指定账号信息。
     */
    @GetMapping("/{sessionName}")
    public ApiResponse<SessionInfo> getAccount(@PathVariable String sessionName) {
        var account = accountManager.getAccount(sessionName);
        if (account == null) {
            return ApiResponse.error("Account not found: " + sessionName);
        }
        SessionInfo info = account.getSessionInfo();
        return ApiResponse.ok(info != null ? info : SessionInfo.builder()
                .sessionName(sessionName).connected(account.isConnected()).build());
    }

    /**
     * 断开账号连接。
     */
    @PostMapping("/{sessionName}/disconnect")
    public ApiResponse<Void> disconnect(@PathVariable String sessionName) {
        boolean ok = accountManager.disconnect(sessionName);
        if (!ok) {
            return ApiResponse.error("Account not found: " + sessionName);
        }
        return ApiResponse.ok("Disconnected", null);
    }

    /**
     * 发送纯文本消息。
     */
    @PostMapping("/{sessionName}/send/text")
    public ApiResponse<Map<String, Object>> sendText(
            @PathVariable String sessionName,
            @RequestBody SendMessageRequest request) {
        try {
            Map<String, Object> result = accountManager.sendTextMessage(
                    sessionName, request.getChatId(), request.getText());
            return ApiResponse.ok("Sent", result);
        } catch (Exception e) {
            log.error("Send text failed", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发送图片消息（通过URL或上传字节数组）。
     */
    @PostMapping("/{sessionName}/send/image")
    public ApiResponse<Map<String, Object>> sendImage(
            @PathVariable String sessionName,
            @RequestBody SendMessageRequest request) {
        try {
            Map<String, Object> result;
            if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
                result = accountManager.sendImageMessage(
                        sessionName, request.getChatId(), request.getImageUrl());
            } else if (request.getImageData() != null && request.getImageData().length > 0) {
                result = accountManager.sendImageMessage(
                        sessionName, request.getChatId(),
                        request.getImageData(), request.getImageFileName());
            } else {
                return ApiResponse.error("Either imageUrl or imageData is required");
            }
            return ApiResponse.ok("Sent", result);
        } catch (Exception e) {
            log.error("Send image failed", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发送 文本+图片 消息（caption模式）。
     */
    @PostMapping("/{sessionName}/send/caption")
    public ApiResponse<Map<String, Object>> sendCaption(
            @PathVariable String sessionName,
            @RequestBody SendMessageRequest request) {
        try {
            String caption = request.getCaption() != null ? request.getCaption() : request.getText();
            if (caption == null || caption.isBlank()) {
                return ApiResponse.error("caption (or text) is required");
            }

            Map<String, Object> result;
            if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
                result = accountManager.sendCaptionMessage(
                        sessionName, request.getChatId(), caption, request.getImageUrl());
            } else if (request.getImageData() != null && request.getImageData().length > 0) {
                result = accountManager.sendCaptionMessage(
                        sessionName, request.getChatId(), caption,
                        request.getImageData(), request.getImageFileName());
            } else {
                return ApiResponse.error("Either imageUrl or imageData is required");
            }
            return ApiResponse.ok("Sent", result);
        } catch (Exception e) {
            log.error("Send caption failed", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
