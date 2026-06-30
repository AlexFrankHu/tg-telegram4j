package com.tg.telegram4j.service;

import com.tg.telegram4j.config.TelegramProperties;
import com.tg.telegram4j.model.SessionInfo;
import com.tg.telegram4j.session.TelethonSessionData;
import com.tg.telegram4j.session.TelethonSessionReader;
import com.tg.telegram4j.store.TelethonImportStoreLayout;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import telegram4j.core.MTProtoTelegramClient;
import telegram4j.core.auth.AuthorizationHandler;
import telegram4j.core.retriever.EntityRetrievalStrategy;
import telegram4j.core.retriever.PreferredEntityRetriever;
import telegram4j.core.event.domain.message.SendMessageEvent;
import telegram4j.core.object.Message;
import telegram4j.core.object.chat.Chat;
import telegram4j.core.spec.SendMessageSpec;
import telegram4j.core.util.Id;
import telegram4j.core.util.PeerId;
import telegram4j.mtproto.resource.ProxyResources;
import telegram4j.mtproto.resource.SocksProxyResources;
import telegram4j.mtproto.resource.TcpClientResources;
import telegram4j.mtproto.store.StoreLayoutImpl;
import telegram4j.tl.BaseUser;
import telegram4j.tl.InputUserSelf;
import telegram4j.tl.User;
import telegram4j.tl.auth.BaseAuthorization;
import telegram4j.tl.request.users.ImmutableGetUsers;

import java.io.File;
import java.net.InetSocketAddress;
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

    /** 活跃客户端，以会话名称为键。 */
    private final Map<String, ClientHolder> clients = new ConcurrentHashMap<>();

    public TelegramClientService(TelegramProperties properties) {
        this.properties = properties;
    }

    /**
     * 应用启动时自动登录（如果配置了 sessionName 和 sessionFilePath）。
     */
    @PostConstruct
    public void autoLogin() {
        String sessionName = properties.getSessionName();
        String sessionFilePath = properties.getSessionFilePath();
        if (sessionName != null && !sessionName.isBlank()
                && sessionFilePath != null && !sessionFilePath.isBlank()) {
            log.info("Auto-login: sessionName={}, sessionFilePath={}", sessionName, sessionFilePath);
            try {
                SessionInfo info = loginFromFile(sessionName, sessionFilePath, null, null);
                log.info("Auto-login successful: userId={}, name={}",
                        info.getUserId(), info.getFirstName());
            } catch (Exception e) {
                log.error("Auto-login failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 导入 Telethon .session 文件（通过文件路径）并连接。
     */
    public SessionInfo loginFromFile(String sessionName, String sessionFilePath,
                                     Integer apiId, String apiHash) {
        TelethonSessionData sessionData = TelethonSessionReader.readFromFile(sessionFilePath);
        return doLogin(sessionName, sessionData, resolveApiId(apiId), resolveApiHash(apiHash));
    }

    /**
     * 导入 Telethon .session 文件（通过原始字节数组）并连接。
     */
    public SessionInfo loginFromBytes(String sessionName, byte[] sessionBytes,
                                      Integer apiId, String apiHash) {
        TelethonSessionData sessionData = TelethonSessionReader.readFromBytes(sessionBytes);
        return doLogin(sessionName, sessionData, resolveApiId(apiId), resolveApiHash(apiHash));
    }

    /**
     * 获取所有活跃会话信息。
     */
    public List<SessionInfo> listSessions() {
        List<SessionInfo> result = new ArrayList<>();
        for (var entry : clients.entrySet()) {
            result.add(buildSessionInfo(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * 获取指定会话的信息。
     */
    public SessionInfo getSession(String sessionName) {
        ClientHolder holder = clients.get(sessionName);
        if (holder == null) {
            return null;
        }
        return buildSessionInfo(sessionName, holder);
    }

    /**
     * 断开指定会话。
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
     * 断开所有会话（应用关闭时调用）。
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

        // 确保数据目录存在
        File dataDir = new File(properties.getDataDir());
        dataDir.mkdirs();

        // 创建自定义的 StoreLayout，注入 .session 文件中的 auth_key
        Path t4jBinPath = Path.of(properties.getDataDir(), sessionName + ".t4j.bin");
        TelethonImportStoreLayout storeLayout = new TelethonImportStoreLayout(
                new StoreLayoutImpl(Function.identity()),
                t4jBinPath,
                sessionData.getDcId(),
                sessionData.getAuthKey()
        );

        // 备用的认证处理器。
        // 如果 .session 中的 auth_key 有效，这个处理器不会被调用。
        // 如果被调用了（401），说明 session 已过期。
        AuthorizationHandler fallbackHandler = resources ->
                Mono.error(new RuntimeException(
                        "Session auth_key is invalid or expired. " +
                        "The .session file cannot be used for direct login. " +
                        "Please generate a new .session file with Telethon."));

        try {
            var bootstrap = MTProtoTelegramClient.create(apiId, apiHash, fallbackHandler)
                    .setStoreLayout(storeLayout)
                    .setEntityRetrieverStrategy(EntityRetrievalStrategy.preferred(
                            EntityRetrievalStrategy.STORE_FALLBACK_RPC,
                            PreferredEntityRetriever.Setting.FULL,
                            PreferredEntityRetriever.Setting.FULL));

            // 配置 SOCKS5 代理（如果开启）
            TelegramProperties.Proxy proxyConfig = properties.getProxy();
            if (proxyConfig.isEnabled()) {
                log.info("Using SOCKS5 proxy: {}:{}", proxyConfig.getHost(), proxyConfig.getPort());
                SocksProxyResources.ProxySpec proxySpec = ProxyResources.ofSocks5()
                        .address(new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()));
                if (proxyConfig.getUsername() != null && !proxyConfig.getUsername().isBlank()) {
                    proxySpec.username(proxyConfig.getUsername());
                }
                if (proxyConfig.getPassword() != null && !proxyConfig.getPassword().isBlank()) {
                    proxySpec.password(proxyConfig.getPassword());
                }
                TcpClientResources tcpResources = TcpClientResources.builder()
                        .proxyResources(proxySpec.build())
                        .build();
                bootstrap.setTcpClientResources(tcpResources);
            }

            MTProtoTelegramClient client = bootstrap
                    .connect()
                    .block(Duration.ofSeconds(30));

            if (client == null) {
                throw new RuntimeException("Failed to connect — client is null");
            }

            // 获取自身用户信息
            Id selfId = client.getSelfId();
            log.info("Session '{}' connected successfully, selfId={}", sessionName, selfId);

            // 更新 store 中的 selfId，确保正确持久化
            storeLayout.updateSelfId(selfId.asLong());

            // 订阅接收消息
            subscribeMessages(sessionName, client);

            // 获取完整用户详情
            SessionInfo info = fetchSelfInfo(sessionName, client, selfId);

            clients.put(sessionName, new ClientHolder(client, storeLayout, info));

            return info;

        } catch (Exception e) {
            log.error("Failed to login session '{}': {}", sessionName, e.getMessage(), e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    private void subscribeMessages(String sessionName, MTProtoTelegramClient client) {
        client.on(SendMessageEvent.class)
                .subscribe(event -> {
                    Message msg = event.getMessage();
                    String text = msg.getContent();
                    Id chatId = msg.getChatId();
                    String authorInfo = msg.getAuthorId()
                            .map(id -> String.valueOf(id.asLong()))
                            .orElse("unknown");
                    String chatName = event.getChat()
                            .map(Chat::getName)
                            .orElse(String.valueOf(chatId.asLong()));
                    log.info("[{}] New message in '{}' from {}: {}",
                            sessionName, chatName, authorInfo, text);
                }, error -> {
                    log.error("[{}] Error in message subscription: {}", sessionName, error.getMessage());
                });
        log.info("Session '{}' subscribed to incoming messages", sessionName);
    }

    /**
     * 发送文本消息到聊天/用户。
     *
     * @param sessionName 发送消息的会话名称
     * @param chatId      目标聊天/用户ID（数字ID或@用户名）
     * @param text        消息文本
     * @return 发送结果信息
     */
    public Map<String, Object> sendMessage(String sessionName, String chatId, String text) {
        ClientHolder holder = clients.get(sessionName);
        if (holder == null) {
            throw new IllegalStateException("Session '" + sessionName + "' is not connected");
        }

        MTProtoTelegramClient client = holder.client;

        try {
            // 解析对端：先尝试数字ID，再尝试用户名
            PeerId peerId;
            try {
                long numericId = Long.parseLong(chatId);
                peerId = PeerId.of(Id.ofUser(numericId));
            } catch (NumberFormatException e) {
                // 当作用户名处理（带或不带@）
                String username = chatId.startsWith("@") ? chatId.substring(1) : chatId;
                peerId = PeerId.of(username);
            }

            Chat chat = client.getChatById(peerId.asId().orElseThrow(
                    () -> new RuntimeException("Cannot resolve peer: " + chatId)))
                    .switchIfEmpty(Mono.error(new RuntimeException("Chat not found: " + chatId)))
                    .block(Duration.ofSeconds(10));

            if (chat == null) {
                throw new RuntimeException("Chat not found: " + chatId);
            }

            Message sent = chat.sendMessage(SendMessageSpec.of(text))
                    .block(Duration.ofSeconds(10));

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("messageId", sent != null ? sent.getId() : -1);
            result.put("chatId", chatId);
            result.put("text", text);
            result.put("timestamp", java.time.Instant.now().toString());
            return result;

        } catch (Exception e) {
            log.error("Failed to send message from session '{}' to '{}': {}",
                    sessionName, chatId, e.getMessage(), e);
            throw new RuntimeException("Send message failed: " + e.getMessage(), e);
        }
    }

    private SessionInfo fetchSelfInfo(String sessionName, MTProtoTelegramClient client, Id selfId) {
        try {
            List<User> users = client.getServiceHolder()
                    .getUserService()
                    .getUsers(List.of(InputUserSelf.instance()))
                    .block(Duration.ofSeconds(10));

            if (users != null && !users.isEmpty() && users.get(0) instanceof BaseUser) {
                BaseUser self = (BaseUser) users.get(0);
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

    // Telegram Desktop 公开的 API 凭证（与 Telethon 内置默认值相同）
    private static final int DEFAULT_API_ID = 2040;
    private static final String DEFAULT_API_HASH = "b18441a1ff607e10a989891a5462e627";

    private int resolveApiId(Integer apiId) {
        if (apiId != null && apiId > 0) {
            return apiId;
        }
        if (properties.getApiId() > 0) {
            return properties.getApiId();
        }
        return DEFAULT_API_ID;
    }

    private String resolveApiHash(String apiHash) {
        if (apiHash != null && !apiHash.isBlank()) {
            return apiHash;
        }
        if (properties.getApiHash() != null && !properties.getApiHash().isBlank()) {
            return properties.getApiHash();
        }
        return DEFAULT_API_HASH;
    }

    private static class ClientHolder {
        private final MTProtoTelegramClient client;
        private final TelethonImportStoreLayout storeLayout;
        private final SessionInfo cachedInfo;

        ClientHolder(MTProtoTelegramClient client,
                     TelethonImportStoreLayout storeLayout,
                     SessionInfo cachedInfo) {
            this.client = client;
            this.storeLayout = storeLayout;
            this.cachedInfo = cachedInfo;
        }
    }
}
