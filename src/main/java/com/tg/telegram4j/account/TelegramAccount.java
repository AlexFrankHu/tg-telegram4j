package com.tg.telegram4j.account;

import com.tg.telegram4j.model.DeviceInfo;
import com.tg.telegram4j.model.ProxyInfo;
import com.tg.telegram4j.model.SessionInfo;
import com.tg.telegram4j.model.TelegramMessage;
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

import telegram4j.tl.ImmutableBaseInputUser;
import telegram4j.tl.ImmutableInputContact;
import telegram4j.tl.InputContact;
import telegram4j.tl.contacts.ImportedContacts;
import telegram4j.tl.request.contacts.ImmutableAddContact;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    private final TelegramEventListener eventListener;

    public TelegramAccount(String sessionName, String dataDir,
                           ProxyInfo proxyInfo, DeviceInfo deviceInfo,
                           boolean autoReadMessages,
                           Integer apiId, String apiHash,
                           TelegramEventListener eventListener) {
        this.sessionName = sessionName;
        this.dataDir = dataDir;
        this.proxyInfo = proxyInfo;
        this.deviceInfo = deviceInfo;
        this.autoReadMessages = autoReadMessages;
        this.apiId = resolveApiId(apiId);
        this.apiHash = resolveApiHash(apiHash);
        this.eventListener = eventListener;
    }

    /**
     * 使用 session 字节数组登录。
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

            // 设置设备指纹（如果提供了的话）
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

            // 设置独立的 SOCKS5 代理（如果提供了的话）
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

            // 订阅接收消息
            subscribeMessages();

            // 订阅断连事件
            subscribeDisconnect();

            // 获取自身账号信息
            sessionInfo = fetchSelfInfo(selfId);
            connected = true;

            return sessionInfo;

        } catch (Exception e) {
            log.error("[{}] Login failed: {}", sessionName, e.getMessage(), e);
            throw new RuntimeException("Login failed for '" + sessionName + "': " + e.getMessage(), e);
        }
    }

    /**
     * 发送纯文本消息。
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
     * 发送纯图片消息（通过图片URL）。
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
     * 发送纯图片消息（通过图片字节数组上传）。
     */
    public Map<String, Object> sendImageMessage(String chatId, byte[] imageData, String fileName) {
        checkConnected();
        try {
            Chat chat = resolveChat(chatId);

            // 写到临时文件，上传后删除
            Path tempFile = Files.createTempFile("tg_upload_", "_" + (fileName != null ? fileName : "photo.jpg"));
            Files.write(tempFile, imageData);

            InputFile inputFile = client.uploadFile(tempFile, fileName != null ? fileName : "photo.jpg", -1)
                    .block(Duration.ofSeconds(60));
            Files.deleteIfExists(tempFile);

            if (inputFile == null) {
                throw new RuntimeException("文件上传返回空");
            }

            InputMediaUploadedPhotoSpec photoSpec = InputMediaUploadedPhotoSpec.of(inputFile);
            SendMessageSpec spec = SendMessageSpec.of("")
                    .withMedia(photoSpec);
            Message sent = chat.sendMessage(spec)
                    .block(Duration.ofSeconds(15));
            return buildSendResult(sent, chatId, "[上传图片]");
        } catch (Exception e) {
            log.error("[{}] 发送上传图片到 '{}' 失败: {}", sessionName, chatId, e.getMessage(), e);
            throw new RuntimeException("发送图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 文本+图片 消息（caption模式，通过图片URL）。
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
     * 发送 文本+图片 消息（caption模式，通过上传图片字节数组）。
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
                throw new RuntimeException("文件上传返回空");
            }

            InputMediaUploadedPhotoSpec photoSpec = InputMediaUploadedPhotoSpec.of(inputFile);
            SendMessageSpec spec = SendMessageSpec.of(caption)
                    .withMedia(photoSpec);
            Message sent = chat.sendMessage(spec)
                    .block(Duration.ofSeconds(15));
            return buildSendResult(sent, chatId, caption + " [上传图片]");
        } catch (Exception e) {
            log.error("[{}] 发送caption到 '{}' 失败: {}", sessionName, chatId, e.getMessage(), e);
            throw new RuntimeException("发送caption失败: " + e.getMessage(), e);
        }
    }

    /**
     * 普通模式添加好友（通过已知的 Telegram 用户）。
     * 使用 contacts.addContact TL 方法，需要目标用户的 user_id 和 access_hash。
     *
     * @param targetUserId 目标用户的 user_id（通过 username 解析或直接传入）
     * @param firstName    备注名-姓
     * @param lastName     备注名-名
     * @param phone        对方手机号（可为空字符串）
     * @return 添加结果信息
     */
    public Map<String, Object> addContact(String targetUserId, String firstName, String lastName, String phone) {
        checkConnected();
        try {
            // 先解析目标用户
            long userId;
            long accessHash = 0;

            try {
                userId = Long.parseLong(targetUserId);
                // 如果是数字ID，尝试从 store 获取 access_hash
                telegram4j.core.object.User user = client.getUserById(Id.ofUser(userId))
                        .block(Duration.ofSeconds(10));
                if (user != null) {
                    accessHash = user.getId().getAccessHash().orElse(0L);
                }
            } catch (NumberFormatException e) {
                // 当做 username 解析
                String username = targetUserId.startsWith("@") ? targetUserId.substring(1) : targetUserId;
                telegram4j.core.object.User user = client.getUserById(PeerId.of(username).asId()
                        .orElseThrow(() -> new RuntimeException("Cannot resolve username: " + targetUserId)))
                        .block(Duration.ofSeconds(10));
                if (user == null) {
                    throw new RuntimeException("User not found: " + targetUserId);
                }
                userId = user.getId().asLong();
                accessHash = user.getId().getAccessHash().orElse(0L);
            }

            // 构造 contacts.addContact 请求
            var request = ImmutableAddContact.builder()
                    .id(ImmutableBaseInputUser.of(userId, accessHash))
                    .firstName(firstName != null ? firstName : "")
                    .lastName(lastName != null ? lastName : "")
                    .phone(phone != null ? phone : "")
                    .build();

            // 通过 UserService 发送请求
            client.getServiceHolder().getUserService().addContact(request)
                    .block(Duration.ofSeconds(15));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("firstName", firstName);
            result.put("lastName", lastName);
            result.put("timestamp", Instant.now().toString());
            log.info("[{}] 添加好友成功: userId={}", sessionName, userId);
            return result;

        } catch (Exception e) {
            log.error("[{}] 添加好友失败: target={}, error={}", sessionName, targetUserId, e.getMessage(), e);
            throw new RuntimeException("添加好友失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过上传通讯录方式批量添加好友。
     * 使用 contacts.importContacts TL 方法，通过手机号匹配已注册的 Telegram 用户。
     *
     * @param contacts 待导入的联系人列表，每个包含 phone、firstName、lastName
     * @return 导入结果（已导入的用户列表、重试列表等）
     */
    public Map<String, Object> importContacts(List<Map<String, String>> contacts) {
        checkConnected();
        try {
            if (contacts == null || contacts.isEmpty()) {
                throw new IllegalArgumentException("联系人列表不能为空");
            }

            // 构造 InputContact 列表
            List<InputContact> inputContacts = new ArrayList<>();
            for (int i = 0; i < contacts.size(); i++) {
                Map<String, String> contact = contacts.get(i);
                String contactPhone = contact.getOrDefault("phone", "");
                String contactFirstName = contact.getOrDefault("firstName", "");
                String contactLastName = contact.getOrDefault("lastName", "");

                if (contactPhone.isBlank()) {
                    continue;
                }

                inputContacts.add(ImmutableInputContact.of(
                        (long) i,  // client_id，用于关联导入结果
                        contactPhone,
                        contactFirstName,
                        contactLastName
                ));
            }

            if (inputContacts.isEmpty()) {
                throw new IllegalArgumentException("没有有效的联系人（需要包含手机号）");
            }

            // 通过 UserService 发送请求
            ImportedContacts result = client.getServiceHolder().getUserService()
                    .importContacts(inputContacts)
                    .block(Duration.ofSeconds(30));

            // 构建返回结果
            Map<String, Object> response = new LinkedHashMap<>();
            if (result != null) {
                // 已成功导入的联系人
                List<Map<String, Object>> imported = new ArrayList<>();
                result.imported().forEach(ic -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("userId", ic.userId());
                    item.put("clientId", ic.clientId());
                    imported.add(item);
                });
                response.put("imported", imported);
                response.put("importedCount", imported.size());

                // 需要重试的联系人
                response.put("retryContacts", result.retryContacts());
                response.put("retryCount", result.retryContacts().size());

                // 返回的用户信息
                List<Map<String, Object>> users = new ArrayList<>();
                result.users().forEach(user -> {
                    if (user instanceof BaseUser ) {
                        BaseUser baseUser = (BaseUser) user;
                        Map<String, Object> userInfo = new LinkedHashMap<>();
                        userInfo.put("userId", baseUser.id());
                        userInfo.put("firstName", baseUser.firstName());
                        userInfo.put("lastName", baseUser.lastName());
                        userInfo.put("username", baseUser.username());
                        userInfo.put("phone", baseUser.phone());
                        users.add(userInfo);
                    }
                });
                response.put("users", users);
            } else {
                response.put("imported", List.of());
                response.put("importedCount", 0);
                response.put("retryContacts", List.of());
                response.put("retryCount", 0);
                response.put("users", List.of());
            }

            response.put("totalRequested", inputContacts.size());
            response.put("timestamp", Instant.now().toString());

            log.info("[{}] 导入通讯录完成: 请求{}个, 成功导入{}个",
                    sessionName, inputContacts.size(), response.get("importedCount"));
            return response;

        } catch (Exception e) {
            log.error("[{}] 导入通讯录失败: {}", sessionName, e.getMessage(), e);
            throw new RuntimeException("导入通讯录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 断开该账号的连接。
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

    // ---- 私有方法 ----

    private void subscribeMessages() {
        client.on(SendMessageEvent.class)
                .subscribe(event -> {
                    Message msg = event.getMessage();
                    Id chatId = msg.getChatId();
                    String messageType = getMessageType(msg);

                    // 发送者信息
                    long senderId = msg.getAuthorId()
                            .map(Id::asLong)
                            .orElse(0L);
                    String senderType = msg.getAuthorId()
                            .map(id -> id.getType().name())
                            .orElse("UNKNOWN");

                    // 聊天信息
                    String chatName = event.getChat()
                            .map(Chat::getName)
                            .orElse("N/A");
                    String chatType = event.getChat()
                            .map(c -> c.getType().name())
                            .orElse("UNKNOWN");

                    // 发送者详细信息
                    String senderUsername = event.getAuthor()
                            .flatMap(MentionablePeer::getUsername)
                            .orElse("");
                    String senderName = event.getAuthor()
                            .map(author -> {
                                if (author instanceof telegram4j.core.object.User u) {
                                    return u.getFullName();
                                }
                                return senderUsername;
                            })
                            .orElse("");

                    // 判断是否是自己发送的
                    boolean outgoing = senderId == client.getSelfId().asLong();

                    log.info("[{}] === 收到新消息 ===", sessionName);
                    log.info("[{}]   消息类型: {}, 聊天: {}({}), 发送者: {}({}), 内容: {}",
                            sessionName, messageType, chatName, chatType,
                            senderUsername, senderId, msg.getContent());

                    // 封装消息体
                    TelegramMessage telegramMessage = TelegramMessage.builder()
                            .messageId(msg.getId())
                            .chatId(chatId.asLong())
                            .chatType(chatType)
                            .chatName(chatName)
                            .senderId(senderId)
                            .senderType(senderType)
                            .senderUsername(senderUsername)
                            .senderName(senderName)
                            .text(msg.getContent())
                            .messageType(messageType)
                            .outgoing(outgoing)
                            .timestamp(Instant.now())
                            .rawMessage(msg)
                            .build();

                    // 通过回调通知 TelegramAccountManager
                    if (eventListener != null) {
                        try {
                            eventListener.onMessage(this, telegramMessage);
                        } catch (Exception e) {
                            log.warn("[{}] 消息回调处理异常: {}", sessionName, e.getMessage());
                        }
                    }

                    // 如果开启了自动已读，则标记消息为已读
                    if (autoReadMessages) {
                        markAsRead(chatId, msg.getId());
                    }
                }, error -> {
                    log.error("[{}] Message subscription error: {}", sessionName, error.getMessage());
                });
        log.info("[{}] Subscribed to incoming messages (autoRead={})", sessionName, autoReadMessages);
    }

    /**
     * 订阅断连事件。
     * 当客户端因网络异常、封号、服务端断开等原因失去连接时，通过回调通知 TelegramAccountManager。
     */
    private void subscribeDisconnect() {
        client.onDisconnect()
                .subscribe(
                        null,
                        error -> {
                            // 异常导致的断连
                            String reason = "连接异常: " + error.getMessage();
                            log.warn("[{}] 账号断连(异常): {}", sessionName, reason);
                            connected = false;
                            if (eventListener != null) {
                                try {
                                    eventListener.onDisconnect(this, reason);
                                } catch (Exception e) {
                                    log.warn("[{}] 断连回调处理异常: {}", sessionName, e.getMessage());
                                }
                            }
                        },
                        () -> {
                            // 正常断连（主动调用 disconnect 或服务端关闭）
                            String reason = "连接已关闭";
                            log.info("[{}] 账号断连(正常): {}", sessionName, reason);
                            connected = false;
                            if (eventListener != null) {
                                try {
                                    eventListener.onDisconnect(this, reason);
                                } catch (Exception e) {
                                    log.warn("[{}] 断连回调处理异常: {}", sessionName, e.getMessage());
                                }
                            }
                        }
                );
        log.info("[{}] Subscribed to disconnect events", sessionName);
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
