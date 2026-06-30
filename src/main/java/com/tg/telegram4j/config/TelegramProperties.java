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

    /** Session name for auto-login on startup */
    private String sessionName;
    /** Path to .session file for auto-login on startup */
    private String sessionFilePath;

    /** SOCKS5 proxy config (for environments that cannot access Telegram directly) */
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
