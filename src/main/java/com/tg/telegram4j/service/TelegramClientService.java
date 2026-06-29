package com.tg.telegram4j.service;

import com.tg.telegram4j.config.TelegramProperties;
import com.tg.telegram4j.model.SessionInfo;
import com.tg.telegram4j.session.TelethonSessionData;
import com.tg.telegram4j.session.TelethonSessionReader;
import com.tg.telegram4j.store.TelethonImportStoreLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import telegram4j.core.MTProtoTelegramClient;
import telegram4j.core.auth.AuthorizationHandler;
import telegram4j.core.retriever.EntityRetrievalStrategy;
import telegram4j.core.retriever.PreferredEntityRetriever;
import telegram4j.core.util.Id;
import telegram4j.mtproto.store.StoreLayoutImpl;
import telegram4j.tl.BaseUser;
import telegram4j.tl.InputUserSelf;
import telegram4j.tl.User;
import telegram4j.tl.auth.BaseAuthorization;
import telegram4j.tl.request.users.ImmutableGetUsers;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Service
public class TelegramClientService {

    private final TelegramProperties properties;

    /** Active clients keyed by session name. */
    private final Map<String, ClientHolder> clients = new ConcurrentHashMap<>();

    public TelegramClientService(TelegramProperties properties) {
        this.properties = properties;
    }

    /**
     * Import a Telethon .session file (from file path) and connect.
     */
    public SessionInfo loginFromFile(String sessionName, String sessionFilePath,
                                     Integer apiId, String apiHash) {
        TelethonSessionData sessionData = TelethonSessionReader.readFromFile(sessionFilePath);
        return doLogin(sessionName, sessionData, resolveApiId(apiId), resolveApiHash(apiHash));
    }

    /**
     * Import a Telethon .session file (from raw bytes) and connect.
     */
    public SessionInfo loginFromBytes(String sessionName, byte[] sessionBytes,
                                      Integer apiId, String apiHash) {
        TelethonSessionData sessionData = TelethonSessionReader.readFromBytes(sessionBytes);
        return doLogin(sessionName, sessionData, resolveApiId(apiId), resolveApiHash(apiHash));
    }

    /**
     * Get info about all active sessions.
     */
    public List<SessionInfo> listSessions() {
        List<SessionInfo> result = new ArrayList<>();
        for (var entry : clients.entrySet()) {
            result.add(buildSessionInfo(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * Get info about a specific session.
     */
    public SessionInfo getSession(String sessionName) {
        ClientHolder holder = clients.get(sessionName);
        if (holder == null) {
            return null;
        }
        return buildSessionInfo(sessionName, holder);
    }

    /**
     * Disconnect a session.
     */
    public boolean disconnect(String sessionName) {
        ClientHolder holder = clients.remove(sessionName);
        if (holder == null) {
            return false;
        }
        try {
            holder.client.disconnect().block(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.warn("Error disconnecting session {}: {}", sessionName, e.getMessage());
        }
        return true;
    }

    /**
     * Disconnect all sessions (called on app shutdown).
     */
    public void disconnectAll() {
        for (String name : clients.keySet()) {
            disconnect(name);
        }
    }

    private SessionInfo doLogin(String sessionName, TelethonSessionData sessionData,
                                int apiId, String apiHash) {
        if (clients.containsKey(sessionName)) {
            throw new IllegalStateException("Session '" + sessionName + "' is already connected");
        }

        log.info("Importing Telethon session '{}': dc_id={}", sessionName, sessionData.getDcId());

        // Ensure data directory exists
        File dataDir = new File(properties.getDataDir());
        dataDir.mkdirs();

        // Create the custom store layout that injects the auth_key from .session
        Path t4jBinPath = Path.of(properties.getDataDir(), sessionName + ".t4j.bin");
        TelethonImportStoreLayout storeLayout = new TelethonImportStoreLayout(
                new StoreLayoutImpl(Function.identity()),
                t4jBinPath,
                sessionData.getDcId(),
                sessionData.getAuthKey()
        );

        // AuthorizationHandler that returns empty Mono — this is a fallback.
        // If the auth_key from .session is valid, this handler is never invoked.
        // If it IS invoked (401), it means the session has expired.
        AuthorizationHandler fallbackHandler = resources ->
                Mono.error(new RuntimeException(
                        "Session auth_key is invalid or expired. " +
                        "The .session file cannot be used for direct login. " +
                        "Please generate a new .session file with Telethon."));

        try {
            MTProtoTelegramClient client = MTProtoTelegramClient.create(apiId, apiHash, fallbackHandler)
                    .setStoreLayout(storeLayout)
                    .setEntityRetrieverStrategy(EntityRetrievalStrategy.preferred(
                            EntityRetrievalStrategy.STORE_FALLBACK_RPC,
                            PreferredEntityRetriever.Setting.FULL,
                            PreferredEntityRetriever.Setting.FULL))
                    .connect()
                    .block(Duration.ofSeconds(30));

            if (client == null) {
                throw new RuntimeException("Failed to connect — client is null");
            }

            // Fetch self user info
            Id selfId = client.getSelfId();
            log.info("Session '{}' connected successfully, selfId={}", sessionName, selfId);

            // Update selfId in store for proper persistence
            storeLayout.updateSelfId(selfId.asLong());

            // Fetch full user details
            SessionInfo info = fetchSelfInfo(sessionName, client, selfId);

            clients.put(sessionName, new ClientHolder(client, storeLayout, info));

            return info;

        } catch (Exception e) {
            log.error("Failed to login session '{}': {}", sessionName, e.getMessage(), e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    private SessionInfo fetchSelfInfo(String sessionName, MTProtoTelegramClient client, Id selfId) {
        try {
            List<User> users = client.getServiceHolder()
                    .getUserService()
                    .getUsers(List.of(InputUserSelf.instance()))
                    .block(Duration.ofSeconds(10));

            if (users != null && !users.isEmpty() && users.get(0) instanceof BaseUser self) {
                String username = self.username();

                return SessionInfo.builder()
                        .sessionName(sessionName)
                        .userId(self.id())
                        .firstName(self.firstName())
                        .lastName(self.lastName() != null ? self.lastName() : "")
                        .username(username)
                        .phone(self.phone() != null ? self.phone() : "")
                        .connected(true)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch self user info: {}", e.getMessage());
        }

        return SessionInfo.builder()
                .sessionName(sessionName)
                .userId(selfId.asLong())
                .connected(true)
                .build();
    }

    private SessionInfo buildSessionInfo(String sessionName, ClientHolder holder) {
        SessionInfo cached = holder.cachedInfo;
        if (cached != null) {
            return cached;
        }
        return SessionInfo.builder()
                .sessionName(sessionName)
                .connected(true)
                .build();
    }

    private int resolveApiId(Integer apiId) {
        if (apiId != null && apiId > 0) {
            return apiId;
        }
        if (properties.getApiId() > 0) {
            return properties.getApiId();
        }
        throw new IllegalArgumentException("apiId is required (provide in request or configure in application.yml)");
    }

    private String resolveApiHash(String apiHash) {
        if (apiHash != null && !apiHash.isBlank()) {
            return apiHash;
        }
        if (properties.getApiHash() != null && !properties.getApiHash().isBlank()) {
            return properties.getApiHash();
        }
        throw new IllegalArgumentException("apiHash is required (provide in request or configure in application.yml)");
    }

    private record ClientHolder(
            MTProtoTelegramClient client,
            TelethonImportStoreLayout storeLayout,
            SessionInfo cachedInfo
    ) {}
}
