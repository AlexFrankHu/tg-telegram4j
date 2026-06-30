package com.tg.telegram4j.controller;

import com.tg.telegram4j.model.ApiResponse;
import com.tg.telegram4j.model.LoginRequest;
import com.tg.telegram4j.model.SessionInfo;
import com.tg.telegram4j.service.TelegramClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final TelegramClientService clientService;

    public SessionController(TelegramClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * 通过上传 .session 文件登录。
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/session/upload \
     *   -F "file=@/path/to/account.session" \
     *   -F "sessionName=my_account"
     * </pre>
     * apiId 和 apiHash 可选 —— 默认使用 Telegram Desktop 的公开凭证。
     */
    @PostMapping("/upload")
    public ApiResponse<SessionInfo> loginByUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionName") String sessionName,
            @RequestParam(value = "apiId", required = false) Integer apiId,
            @RequestParam(value = "apiHash", required = false) String apiHash) {
        try {
            byte[] sessionBytes = file.getBytes();
            SessionInfo info = clientService.loginFromBytes(sessionName, sessionBytes, apiId, apiHash);
            return ApiResponse.ok("Login successful", info);
        } catch (Exception e) {
            log.error("Upload login failed", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 通过服务器上的 .session 文件路径登录。
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/session/import \
     *   -H "Content-Type: application/json" \
     *   -d '{"sessionName":"my_account","sessionFilePath":"/data/account.session"}'
     * </pre>
     * apiId 和 apiHash 可选 —— 默认使用 Telegram Desktop 的公开凭证。
     */
    @PostMapping("/import")
    public ApiResponse<SessionInfo> loginByPath(@RequestBody LoginRequest request) {
        try {
            SessionInfo info = clientService.loginFromFile(
                    request.getSessionName(),
                    request.getSessionFilePath(),
                    request.getApiId(),
                    request.getApiHash());
            return ApiResponse.ok("Login successful", info);
        } catch (Exception e) {
            log.error("Path login failed", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有活跃会话列表。
     */
    @GetMapping("/list")
    public ApiResponse<List<SessionInfo>> listSessions() {
        return ApiResponse.ok(clientService.listSessions());
    }

    /**
     * 获取指定会话的信息。
     */
    @GetMapping("/{sessionName}")
    public ApiResponse<SessionInfo> getSession(@PathVariable String sessionName) {
        SessionInfo info = clientService.getSession(sessionName);
        if (info == null) {
            return ApiResponse.error("Session not found: " + sessionName);
        }
        return ApiResponse.ok(info);
    }

    /**
     * 发送文本消息。
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/session/my_account/send \
     *   -H "Content-Type: application/json" \
     *   -d '{"chatId":"123456789","text":"Hello!"}'
     *
     * # 或者通过用户名发送:
     * curl -X POST http://localhost:8080/api/session/my_account/send \
     *   -H "Content-Type: application/json" \
     *   -d '{"chatId":"@username","text":"Hello!"}'
     * </pre>
     */
    @PostMapping("/{sessionName}/send")
    public ApiResponse<Map<String, Object>> sendMessage(
            @PathVariable String sessionName,
            @RequestBody Map<String, String> body) {
        try {
            String chatId = body.get("chatId");
            String text = body.get("text");
            if (chatId == null || chatId.isBlank()) {
                return ApiResponse.error("chatId is required");
            }
            if (text == null || text.isBlank()) {
                return ApiResponse.error("text is required");
            }
            Map<String, Object> result = clientService.sendMessage(sessionName, chatId, text);
            return ApiResponse.ok("Message sent", result);
        } catch (Exception e) {
            log.error("Send message failed", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 断开指定会话。
     */
    @PostMapping("/{sessionName}/disconnect")
    public ApiResponse<Void> disconnect(@PathVariable String sessionName) {
        boolean ok = clientService.disconnect(sessionName);
        if (!ok) {
            return ApiResponse.error("Session not found: " + sessionName);
        }
        return ApiResponse.ok("Disconnected", null);
    }
}
