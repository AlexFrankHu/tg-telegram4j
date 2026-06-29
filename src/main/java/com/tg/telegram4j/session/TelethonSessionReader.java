package com.tg.telegram4j.session;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reads a Telethon .session file (SQLite database) and extracts
 * the MTProto auth_key and DC information.
 *
 * <p>Telethon session SQLite schema (key table):
 * <pre>
 * CREATE TABLE sessions (
 *   dc_id        integer primary key,
 *   server_address text,
 *   port         integer,
 *   auth_key     blob     -- 256 bytes, the MTProto authorization key
 * );
 * </pre>
 */
@Slf4j
public class TelethonSessionReader {

    private TelethonSessionReader() {}

    /**
     * Read session data from a .session file on disk.
     */
    public static TelethonSessionData readFromFile(String filePath) {
        return readFromSqlite(filePath);
    }

    /**
     * Read session data from raw bytes (e.g. uploaded via REST).
     * Writes to a temp file since SQLite JDBC requires a file path.
     */
    public static TelethonSessionData readFromBytes(byte[] sessionBytes) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("telethon_session_", ".session");
            Files.write(tempFile, sessionBytes);
            return readFromSqlite(tempFile.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write session bytes to temp file", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static TelethonSessionData readFromSqlite(String dbPath) {
        String url = "jdbc:sqlite:" + dbPath;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT dc_id, server_address, port, auth_key FROM sessions")) {

            if (!rs.next()) {
                throw new RuntimeException("No session data found in .session file");
            }

            int dcId = rs.getInt("dc_id");
            String serverAddress = rs.getString("server_address");
            int port = rs.getInt("port");
            byte[] authKey = rs.getBytes("auth_key");

            if (authKey == null || authKey.length != 256) {
                throw new RuntimeException(
                        "Invalid auth_key: expected 256 bytes, got " +
                        (authKey == null ? "null" : authKey.length));
            }

            log.info("Read Telethon session: dc_id={}, server={}:{}, auth_key={} bytes",
                    dcId, serverAddress, port, authKey.length);

            return new TelethonSessionData(dcId, serverAddress, port, authKey);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to read .session SQLite file: " + dbPath, e);
        }
    }
}
