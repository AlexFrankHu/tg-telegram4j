package com.tg.telegram4j.controller;

import com.tg.telegram4j.model.*;
import com.tg.telegram4j.service.TelegramAccountManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final TelegramAccountManager accountManager;

    public AccountController(TelegramAccountManager accountManager) {
        this.accountManager = accountManager;
    }

    /**
     * 登录账号。
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/account/login \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "sessionName": "my_account",
     *     "sessionData": "BASE64_ENCODED_SESSION_BYTES",
     *     "autoReadMessages": true,
     *     "proxy": { "host": "127.0.0.1", "port": 1080 },
     *     "device": { "deviceModel": "Samsung Galaxy S21", "systemVersion": "Android 12", "appVersion": "9.0.0" }
     *   }'
     * </pre>
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
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/account/login/upload \
     *   -F "file=@account.session" \
     *   -F "sessionName=my_account" \
     *   -F "autoReadMessages=true" \
     *   -F "proxyHost=127.0.0.1" \
     *   -F "proxyPort=1080"
     * </pre>
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
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/account/my_account/send/text \
     *   -H "Content-Type: application/json" \
     *   -d '{"chatId":"123456789","text":"Hello!"}'
     * </pre>
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
     *
     * <pre>
     * # 通过URL发送:
     * curl -X POST http://localhost:8080/api/account/my_account/send/image \
     *   -H "Content-Type: application/json" \
     *   -d '{"chatId":"123456789","imageUrl":"https://example.com/photo.jpg"}'
     *
     * # 通过base64图片数据发送:
     * curl -X POST http://localhost:8080/api/account/my_account/send/image \
     *   -H "Content-Type: application/json" \
     *   -d '{"chatId":"123456789","imageData":"BASE64...","imageFileName":"photo.jpg"}'
     * </pre>
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
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/account/my_account/send/caption \
     *   -H "Content-Type: application/json" \
     *   -d '{"chatId":"123456789","caption":"Look at this!","imageUrl":"https://example.com/photo.jpg"}'
     * </pre>
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
