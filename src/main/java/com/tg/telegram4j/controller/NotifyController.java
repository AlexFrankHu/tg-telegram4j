package com.tg.telegram4j.controller;

import com.tg.telegram4j.model.ApiResponse;
import com.tg.telegram4j.service.TelegramNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Telegram 机器人通知测试接口。
 */
@Slf4j
@RestController
@RequestMapping("/api/notify")
public class NotifyController {

    private final TelegramNotifyService notifyService;

    public NotifyController(TelegramNotifyService notifyService) {
        this.notifyService = notifyService;
    }

    /**
     * 发送通知测试接口。
     *
     * <pre>
     * curl -X POST http://localhost:8080/api/notify/send \
     *   -H "Content-Type: application/json" \
     *   -d '{"title":"测试通知","content":"这是一条测试消息"}'
     * </pre>
     */
    @PostMapping("/send")
    public ApiResponse<Map<String, Object>> sendNotify(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");

        if (title == null || title.isBlank()) {
            return ApiResponse.error("title 不能为空");
        }
        if (content == null || content.isBlank()) {
            return ApiResponse.error("content 不能为空");
        }

        boolean success = notifyService.sendNotify(title, content);
        if (success) {
            return ApiResponse.ok("通知发送成功", Map.of("title", title, "sent", true));
        } else {
            return ApiResponse.error("通知发送失败，请检查 bot-token 和 chat-id 配置");
        }
    }
}
