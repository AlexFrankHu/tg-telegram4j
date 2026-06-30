package com.tg.telegram4j.session;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 从 Telethon .session SQLite 文件中提取的数据。
 * <p>
 * .session 文件包含一张 {@code sessions} 表，字段为：
 * dc_id (int), server_address (text), port (int), auth_key (blob 256字节)。
 */
@Data
@AllArgsConstructor
public class TelethonSessionData {
    private int dcId;
    private String serverAddress;
    private int port;
    private byte[] authKey;
}
