package com.tg.telegram4j.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Telegram 机器人通知服务。
 * 通过 Telegram Bot API 将通知消息发送到指定群组。
 */
@Slf4j
@Service
public class TelegramNotifyService {

    @Value("${telegram.notify.bot-token:}")
    private String botToken;

    @Value("${telegram.notify.chat-id:}")
    private String chatId;

    /**
     * 发送通知到 Telegram 群组。
     *
     * @param title   通知标题
     * @param content 通知内容
     * @return 是否发送成功
     */
    public boolean sendNotify(String title, String content) {
        if (botToken == null || botToken.isBlank()) {
            log.warn("[TG通知] bot-token 未配置，无法发送通知");
            return false;
        }
        if (chatId == null || chatId.isBlank()) {
            log.warn("[TG通知] chat-id 未配置，无法发送通知");
            return false;
        }

        // 组装消息文本（HTML格式）
        String message = "<b>" + escapeHtml(title) + "</b>\n\n" + escapeHtml(content);

        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String apiUrl = String.format(
                    "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=HTML",
                    botToken, chatId, encodedMessage
            );

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                log.info("[TG通知] 发送成功: title={}", title);
                return true;
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    log.warn("[TG通知] 发送失败: code={}, response={}", responseCode, response);
                }
                return false;
            }
        } catch (Exception e) {
            log.error("[TG通知] 发送异常: title={}, error={}", title, e.getMessage());
            return false;
        }
    }

    /**
     * HTML特殊字符转义。
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
