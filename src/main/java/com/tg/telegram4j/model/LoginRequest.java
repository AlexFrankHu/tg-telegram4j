package com.tg.telegram4j.model;

import lombok.Data;

@Data
public class LoginRequest {
    /** Unique name for this session (used as storage key) */
    private String sessionName;
    /** Path to the .session file on server disk (alternative to file upload) */
    private String sessionFilePath;
    /** Telegram API ID from https://my.telegram.org/apps */
    private Integer apiId;
    /** Telegram API Hash from https://my.telegram.org/apps */
    private String apiHash;
}
