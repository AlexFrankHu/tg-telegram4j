package com.tg.telegram4j.session;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data extracted from a Telethon .session SQLite file.
 * <p>
 * The .session file contains a {@code sessions} table with columns:
 * dc_id (int), server_address (text), port (int), auth_key (blob 256 bytes).
 */
@Data
@AllArgsConstructor
public class TelethonSessionData {
    private int dcId;
    private String serverAddress;
    private int port;
    private byte[] authKey;
}
