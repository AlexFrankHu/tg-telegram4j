package com.tg.telegram4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "telegram")
public class TelegramProperties {

    private String dataDir = "./data";
    private int apiId = 0;
    private String apiHash = "";

    /** 启动时自动登录的会话名称 */
    private String sessionName;
    /** 启动时自动登录的 .session 文件路径 */
    private String sessionFilePath;

    /** SOCKS5 代理配置（用于无法直接访问 Telegram 的环境） */
    private Proxy proxy = new Proxy();

    @Data
    public static class Proxy {
        private boolean enabled = false;
        private String host = "127.0.0.1";
        private int port = 1080;
        private String username;
        private String password;
    }
}
