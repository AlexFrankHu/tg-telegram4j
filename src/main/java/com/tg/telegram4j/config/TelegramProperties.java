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
}
