package com.tg.telegram4j.model;

import lombok.Data;

@Data
public class LoginRequest {
    /** 会话唯一名称（用作存储键） */
    private String sessionName;
    /** 服务器磁盘上 .session 文件的路径（文件上传的替代方式） */
    private String sessionFilePath;
    /** Telegram API ID（来自 https://my.telegram.org/apps），可选 */
    private Integer apiId;
    /** Telegram API Hash（来自 https://my.telegram.org/apps），可选 */
    private String apiHash;
}
