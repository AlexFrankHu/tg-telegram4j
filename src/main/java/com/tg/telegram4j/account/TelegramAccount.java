package com.tg.telegram4j.account;

import com.tg.telegram4j.model.DeviceInfo;
import com.tg.telegram4j.model.ProxyInfo;
import com.tg.telegram4j.model.SessionInfo;
import com.tg.telegram4j.session.TelethonSessionData;
import com.tg.telegram4j.session.TelethonSessionReader;
import com.tg.telegram4j.store.TelethonImportStoreLayout;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import telegram4j.core.InitConnectionParams;
import telegram4j.core.MTProtoTelegramClient;
import telegram4j.core.auth.AuthorizationHandler;
import telegram4j.core.event.domain.message.SendMessageEvent;
import telegram4j.core.object.Message;
import telegram4j.core.object.MentionablePeer;
import telegram4j.core.object.chat.Chat;
import telegram4j.core.retriever.EntityRetrievalStrategy;
import telegram4j.core.retriever.PreferredEntityRetriever;
import telegram4j.core.spec.SendMessageSpec;
import telegram4j.core.spec.media.InputMediaPhotoSpec;
import telegram4j.core.spec.media.InputMediaUploadedPhotoSpec;
import telegram4j.core.util.Id;
import telegram4j.core.util.PeerId;
import telegram4j.mtproto.DcId;
import telegram4j.mtproto.resource.ProxyResources;
import telegram4j.mtproto.resource.SocksProxyResources;
import telegram4j.mtproto.resource.TcpClientResources;
import telegram4j.mtproto.store.StoreLayoutImpl;
import telegram4j.tl.BaseUser;
import telegram4j.tl.InputFile;
import telegram4j.tl.InputPeer;
import telegram4j.tl.InputUserSelf;
import telegram4j.tl.User;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class TelegramAccount {

    private static final int DEFAULT_API_ID = 2040;
    private static final String DEFAULT_API_HASH = "b18441a1ff607e10a989891a5462e627";

    @Getter
    private final String sessionName;
    @Getter
    private final ProxyInfo proxyInfo;
    @Getter
    private final DeviceInfo deviceInfo;
    @Getter
    private final boolean autoReadMessages;

    private MTProtoTelegramClient client;
    private TelethonImportStoreLayout storeLayout;
    @Getter
    private SessionInfo sessionInfo;
    @Getter
    private boolean connected;

    private final String dataDir;
    private final int apiId;
    private final String apiHash;

    public TelegramAccount(String sessionName, String dataDir,
                           ProxyInfo proxyInfo, DeviceInfo deviceInfo,
                           boolean autoReadMessages,
                           Integer apiId, String apiHash) {
        this.sessionName = sessionName;
        this.dataDir = dataDir;
        this.proxyInfo = proxyInfo;
        this.deviceInfo = deviceInfo;
        this.autoReadMessages = autoReadMessages;
        this.apiId = resolveApiId(apiId);
        this.apiHash = resolveApiHash(apiHash);
    }

    /**
     * Login using session byte array.
     */
    public SessionInfo login(byte[] sessionBytes) {
        if (connected) {
            throw new IllegalStateException("Account '" + sessionName + "' is already connected");
        }

        TelethonSessionData sessionData = TelethonSessionReader.readFromBytes(sessionBytes);
        log.info("[{}] Importing session: dc_id={}", sessionName, sessionData.getDcId());

        File dataDirFile = new File(dataDir);
        dataDirFile.mkdirs();

        Path t4jBinPath = Path.of(dataDir, sessionName + ".t4j.bin");
        storeLayout = new TelethonImportStoreLayout(
                new StoreLayoutImpl(Function.identity()),
                t4jBinPath,
                sessionData.getDcId(),
                sessionData.getAuthKey()
        );

        AuthorizationHandler fallbackHandler = resources ->
                Mono.error(new RuntimeException(
                        "Session auth_key is invalid or expired for '" + sessionName + "'. " +
                        "Please generate a new .session file with Telethon."));

        try {
            var bootstrap = MTProtoTelegramClient.create(apiId, apiHash, fallbackHandler)
                    .setStoreLayout(storeLayout)
                    .setEntityRetrieverStrategy(EntityRetrievalStrategy.preferred(
                            EntityRetrievalStrategy.STORE_FALLBACK_RPC,
                            PreferredEntityRetriever.Setting.FULL,
                            PreferredEntityRetriever.Setting.FULL));

            // Set device fingerprint if provided
            if (deviceInfo != null) {
                InitConnectionParams params = new InitConnectionParams(
                        deviceInfo.getAppVersion() != null ? deviceInfo.getAppVersion() : "1.0.0",
                        deviceInfo.getDeviceModel() != null ? deviceInfo.getDeviceModel() : "Telegram4J",
                        deviceInfo.getLangCode() != null ? deviceInfo.getLangCode() : "en",
                        deviceInfo.getLangPack() != null ? deviceInfo.getLangPack() : "",
                        deviceInfo.getSystemVersion() != null ? deviceInfo.getSystemVersion() : "Windows 10",
                        deviceInfo.getSystemLangCode() != null ? deviceInfo.getSystemLangCode() : "en",
                        null, null
                );
                bootstrap.setInitConnectionParams(params);
            }

            // Set per-account SOCKS5 proxy if provided
            if (proxyInfo != null && proxyInfo.getHost() != null && !proxyInfo.getHost().isBlank()) {
                log.info("[{}] Using SOCKS5 proxy: {}:{}", sessionName, proxyInfo.getHost(), proxyInfo.getPort());
                SocksProxyResources.ProxySpec proxySpec = ProxyResources.ofSocks5()
                        .address(new InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort()));
                if (proxyInfo.getUsername() != null && !proxyInfo.getUsername().isBlank()) {
                    proxySpec.username(proxyInfo.getUsername());
                }
                if (proxyInfo.getPassword() != null && !proxyInfo.getPassword().isBlank()) {
                    proxySpec.password(proxyInfo.getPassword());
                }
                TcpClientResources tcpResources = TcpClientResources.builder()
                        .proxyResources(proxySpec.build())
                        .build();
                bootstrap.setTcpClientResources(tcpResources);
            }

            client = bootstrap.connect().block(Duration.ofSeconds(30));

            if (client == null) {
                throw new RuntimeException("Failed to connect — client is null");
            }

            Id selfId = client.getSelfId();
            log.info("[{}] Connected successfully, selfId={}", sessionName, selfId);

            storeLayout.updateSelfId(selfId.asLong());

            // Subscribe to incoming messages
            subscribeMessages();

            // Fetch self info
            sessionInfo = fetchSelfInfo(selfId);
            connected = true;

            return sessionInfo;

        } catch (Exception e) {
            log.error("[{}] Login failed: {}", sessionName, e.getMessage(), e);
            throw new RuntimeException("Login failed for '" + sessionName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Send a text-only message.
     */
    public Map<String, Object> sendTextMessage(String chatId, String text) {
        checkConnected();
        try {
            Chat chat = resolveChat(chatId);
            Message sent = chat.sendMessage(SendMessageSpec.of(text))
                    .block(Duration.ofSeconds(15));
            return buildSendResult(sent, chatId, text);
        } catch (Exception e) {
            log.error("[{}] Failed to send text to '{}': {}", sessionName, chatId, e.getMessage(), e);
            throw new RuntimeException("Send failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send an image-only message (photo from URL).
     */
    public Map<String, Object> sendImageMessage(String chatId, String imageUrl) {
        checkConnected();
        try {
            Chat chat = resolveChat(chatId);
            InputMediaPhotoSpec photoSpec = InputMediaPhotoSpec.of(imageUrl);
            SendMessageSpec spec = SendMessageSpec.of("")
                    .withMedia(photoSpec);
            Message sent = chat.sendMessage(spec)
                    .block(Duration.ofSeconds(30));
            return buildSendResult(sent, chatId, "[photo: " + imageUrl + "]");
        } catch (Exception e) {
            log.error("[{}] Failed to send image to '{}': {}", sessionName, chatId, e.getMessage(), e);
            throw new RuntimeException("Send image failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send an image from byte[] data.
     */
    public Map<String, Object> sendImageMessage(String chatId, byte[] imageData, String fileName) {
        checkConnected();
        try {
            Chat chat = resolveChat(chatId);

            // Write to temp file, upload, then delete
            Path tempFile = Files.createTempFile("tg_upload_", "_" + (fileName != null ? fileName : "photo.jpg"));
            Files.write(tempFile, imageData);

            InputFile inputFile = client.uploadFile(tempFile, fileName != null ? fileName : "photo.jpg", -1)
                    .block(Duration.ofSeconds(60));
            Files.deleteIfExists(tempFile);

            if (inputFile == null) {
                throw new RuntimeException("File upload returned null");
            }

            InputMediaUploadedPhotoSpec photoSpec = InputMediaUploadedPhotoSpec.of(inputFile);
            SendMessageSpec spec = SendMessageSpec.of("")
                    .withMedia(photoSpec);
            Message sent = chat.sendMessage(spec)
                    .block(Duration.ofSeconds(15));
            return buildSendResult(sent, chatId, "[uploaded photo]");
        } catch (Exception e) {
            log.error("[{}] Failed to send uploaded image to '{}': {}", sessionName, chatId, e.getMessage(), e);
            throw new RuntimeException("Send image failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send a text + image message (caption mode) with image URL.
     */
    public Map<String, Object> sendCaptionMessage(String chatId, String caption, String imageUrl) {
        checkConnected();
        try {
            Chat chat = resolveChat(chatId);
            InputMediaPhotoSpec photoSpec = InputMediaPhotoSpec.of(imageUrl);
            SendMessageSpec spec = SendMessageSpec.of(caption)
                    .withMedia(photoSpec);
            Message sent = chat.sendMessage(spec)
                    .block(Duration.ofSeconds(30));
            return buildSendResult(sent, chatId, caption + " [photo: " + imageUrl + "]");
        } catch (Exception e) {
            log.error("[{}] Failed to send caption to '{}': {}", sessionName, chatId, e.getMessage(), e);
            throw new RuntimeException("Send caption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send a text + image message (caption mode) with uploaded image bytes.
     */
    public Map<String, Object> sendCaptionMessage(String chatId, String caption, byte[] imageData, String fileName) {
        checkConnected();
        try {
            Chat chat = resolveChat(chatId);

            Path tempFile = Files.createTempFile("tg_upload_", "_" + (fileName != null ? fileName : "photo.jpg"));
            Files.write(tempFile, imageData);

            InputFile inputFile = client.uploadFile(tempFile, fileName != null ? fileName : "photo.jpg", -1)
                    .block(Duration.ofSeconds(60));
            Files.deleteIfExists(tempFile);

            if (inputFile == null) {
                throw new RuntimeException("File upload returned null");
            }

            InputMediaUploadedPhotoSpec photoSpec = InputMediaUploadedPhotoSpec.of(inputFile);
            SendMessageSpec spec = SendMessageSpec.of(caption)
                    .withMedia(photoSpec);
            Message sent = chat.sendMessage(spec)
                    .block(Duration.ofSeconds(15));
            return buildSendResult(sent, chatId, caption + " [uploaded photo]");
        } catch (Exception e) {
            log.error("[{}] Failed to send caption to '{}': {}", sessionName, chatId, e.getMessage(), e);
            throw new RuntimeException("Send caption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Disconnect this account.
     */
    public void disconnect() {
        if (client != null) {
            try {
                client.disconnect().block(Duration.ofSeconds(10));
            } catch (Exception e) {
                log.warn("[{}] Error disconnecting: {}", sessionName, e.getMessage());
            }
        }
        connected = false;
        log.info("[{}] Disconnected", sessionName);
    }

    // ---- Private methods ----

    private void subscribeMessages() {
        client.on(SendMessageEvent.class)
                .subscribe(event -> {
                    Message msg = event.getMessage();
                    Id chatId = msg.getChatId();
                    String messageType = getMessageType(msg);

                    // Author info
                    String authorId = msg.getAuthorId()
                            .map(id -> String.valueOf(id.asLong()))
                            .orElse("unknown");
                    String authorType = msg.getAuthorId()
                            .map(id -> id.getType().name())
                            .orElse("UNKNOWN");

                    // Chat info
                    String chatName = event.getChat()
                            .map(Chat::getName)
                            .orElse("N/A");
                    String chatType = event.getChat()
                            .map(c -> c.getType().name())
                            .orElse("UNKNOWN");

                    // Author details from event
                    String authorDetails = event.getAuthor()
                            .map(author -> author.getUsername().orElse("N/A"))
                            .orElse("N/A");

                    log.info("[{}] === New Message ===", sessionName);
                    log.info("[{}]   Type: {}", sessionName, messageType);
                    log.info("[{}]   ChatId: {} ({})", sessionName, chatId.asLong(), chatType);
                    log.info("[{}]   ChatName: {}", sessionName, chatName);
                    log.info("[{}]   AuthorId: {} ({})", sessionName, authorId, authorType);
                    log.info("[{}]   AuthorName: {}", sessionName, authorDetails);
                    log.info("[{}]   Content: {}", sessionName, msg.getContent());
                    log.info("[{}]   MessageId: {}", sessionName, msg.getId());
                    log.info("[{}] ==================", sessionName);

                    // Auto-read if enabled
                    if (autoReadMessages) {
                        markAsRead(chatId, msg.getId());
                    }
                }, error -> {
                    log.error("[{}] Message subscription error: {}", sessionName, error.getMessage());
                });
        log.info("[{}] Subscribed to incoming messages (autoRead={})", sessionName, autoReadMessages);
    }

    private void markAsRead(Id chatId, int messageId) {
        try {
            client.asInputPeer(chatId)
                    .flatMap(peer -> client.getServiceHolder()
                            .getChatService()
                            .readHistory(peer, messageId))
                    .subscribe(
                            result -> log.debug("[{}] Marked as read: chatId={}, msgId={}", sessionName, chatId.asLong(), messageId),
                            error -> log.warn("[{}] Failed to mark as read: {}", sessionName, error.getMessage())
                    );
        } catch (Exception e) {
            log.warn("[{}] Error marking as read: {}", sessionName, e.getMessage());
        }
    }

    private String getMessageType(Message msg) {
        if (msg.getMedia().isPresent()) {
            return "MEDIA";
        }
        if (msg.getContent() != null && !msg.getContent().isEmpty()) {
            return "TEXT";
        }
        return "OTHER";
    }

    private Chat resolveChat(String chatId) {
        PeerId peerId;
        try {
            long numericId = Long.parseLong(chatId);
            peerId = PeerId.of(Id.ofUser(numericId));
        } catch (NumberFormatException e) {
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
        return chat;
    }

    private SessionInfo fetchSelfInfo(Id selfId) {
        try {
            List<User> users = client.getServiceHolder()
                    .getUserService()
                    .getUsers(List.of(InputUserSelf.instance()))
                    .block(Duration.ofSeconds(10));

            if (users != null && !users.isEmpty() && users.get(0) instanceof BaseUser) {
                BaseUser self = (BaseUser) users.get(0);
                return SessionInfo.builder()
                        .sessionName(sessionName)
                        .userId(self.id())
                        .firstName(self.firstName())
                        .lastName(self.lastName() != null ? self.lastName() : "")
                        .username(self.username())
                        .phone(self.phone() != null ? self.phone() : "")
                        .connected(true)
                        .build();
            }
        } catch (Exception e) {
            log.warn("[{}] Failed to fetch self info: {}", sessionName, e.getMessage());
        }
        return SessionInfo.builder()
                .sessionName(sessionName)
                .userId(selfId.asLong())
                .connected(true)
                .build();
    }

    private Map<String, Object> buildSendResult(Message sent, String chatId, String textSummary) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("messageId", sent != null ? sent.getId() : -1);
        result.put("chatId", chatId);
        result.put("text", textSummary);
        result.put("timestamp", Instant.now().toString());
        return result;
    }

    private void checkConnected() {
        if (!connected || client == null) {
            throw new IllegalStateException("Account '" + sessionName + "' is not connected");
        }
    }

    private static int resolveApiId(Integer apiId) {
        return (apiId != null && apiId > 0) ? apiId : DEFAULT_API_ID;
    }

    private static String resolveApiHash(String apiHash) {
        return (apiHash != null && !apiHash.isBlank()) ? apiHash : DEFAULT_API_HASH;
    }
}
