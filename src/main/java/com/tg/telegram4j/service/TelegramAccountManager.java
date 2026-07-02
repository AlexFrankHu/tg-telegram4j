package com.tg.telegram4j.service;

import com.tg.telegram4j.account.TelegramAccount;
import com.tg.telegram4j.account.TelegramEventListener;
import com.tg.telegram4j.entity.TgClusterNode;
import com.tg.telegram4j.entity.TgTelethonAccount;
import com.tg.telegram4j.model.AccountLoginRequest;
import com.tg.telegram4j.model.DeviceInfo;
import com.tg.telegram4j.model.ProxyInfo;
import com.tg.telegram4j.model.SessionInfo;
import com.tg.telegram4j.model.TelegramMessage;
import com.tg.telegram4j.utils.MD5;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class TelegramAccountManager implements TelegramEventListener {

    @Autowired
    private TgTelethonAccountService tgTelethonAccountService;

    @Autowired
    private TgClusterNodeService tgClusterNodeService;

    @Autowired
    private TelegramNotifyService notifyService;


    private String NODE_ID = "";

    private String BASE_DIR = "";
    private String DATA_DIR = "";

    private final TgTelethonAccountService accountService;
    private final Environment environment;
    private final Map<String, TelegramAccount> accounts = new ConcurrentHashMap<>();

    private String PUBLIC_IP = "";
    private String PRIVATE_IP = "";
    private Integer PORT = 0;
    private boolean INIT_SUCCESS = false;

    private String NODE_BASE_INFO = "";


    public TelegramAccountManager(TgTelethonAccountService accountService, Environment environment) {
        this.accountService = accountService;
        this.environment = environment;
    }

    /**
     * 初始化方法，打印当前运行目录等环境信息。
     */
    @PostConstruct
    public void init() {
        String workDir = System.getProperty("user.dir");
        log.info("========== TelegramAccountManager 初始化 ==========");
        log.info("当前运行目录: {}", workDir);
        File workFile = new File(workDir);
        File baseFile = workFile.getParentFile();
        BASE_DIR = baseFile.getAbsolutePath();
        log.info("程序基础目录: {}", BASE_DIR);
        DATA_DIR = BASE_DIR + File.separator + "data";
        log.info("数据基础目录: {}", DATA_DIR);

        // 打印IP地址和端口
        PORT = Integer.parseInt(environment.getProperty("server.port", "8080"));
        PUBLIC_IP = getPublicIp();
        PRIVATE_IP = getPrivateIp();
        log.info("服务端口: {}", PORT);
        log.info("公网IP: {}", PUBLIC_IP);
        log.info("内网IP: {}", PRIVATE_IP);

        log.info("===================================================");
        initNodeInfo();

        StringBuilder nodeInfoStringBuilder = new StringBuilder();
        nodeInfoStringBuilder.append("------------------------------").append("\n");
        nodeInfoStringBuilder.append("节点：").append(NODE_ID).append("\n");
        nodeInfoStringBuilder.append("内网：").append(PRIVATE_IP).append("\n");
        nodeInfoStringBuilder.append("端口：").append(PORT).append("\n");
        nodeInfoStringBuilder.append("------------------------------").append("\n");
        NODE_BASE_INFO = nodeInfoStringBuilder.toString();
        INIT_SUCCESS = true;
    }

    @Scheduled(initialDelay = 20*1000, fixedDelay = 10*1000)
    public void heartbeat() throws InterruptedException {

        log.info("=============>heartbeat");

        if (!INIT_SUCCESS) {
            return ;
        }

        if (NODE_ID == null || NODE_ID.trim().length() <= 0) {
            notifyService.sendNotify("节点错误", NODE_BASE_INFO + "节点ID错误");
            return ;
        }

        Long nodeAccountCount = tgTelethonAccountService.getAccountCount(NODE_ID);

        Date nowDate = new Date();
        TgClusterNode tgClusterNode = tgClusterNodeService.getByNodeId(NODE_ID);
        if (tgClusterNode == null) {
            tgClusterNode = new TgClusterNode();
            tgClusterNode.setNodeId(NODE_ID);
            tgClusterNode.setNodeDir(BASE_DIR);
            tgClusterNode.setPublicIp(PUBLIC_IP);
            tgClusterNode.setPrivateIp(PRIVATE_IP);
            tgClusterNode.setNodePort(PORT);
            tgClusterNode.setNodeStatus("1");
            tgClusterNode.setMaxAccountCount(200);
            tgClusterNode.setCreateTime(nowDate);
            tgClusterNode.setNodeType("Java");
            tgClusterNode.setLastActiveTime(nowDate);
            tgClusterNode.setOnlineAccountCount(accounts.size());
            tgClusterNode.setTotalAccountCount(nodeAccountCount.intValue());
            tgClusterNodeService.insert(tgClusterNode);
        } else {
            TgClusterNode updateTgClusterNode = new TgClusterNode();
            updateTgClusterNode.setNodeId(NODE_ID);
            updateTgClusterNode.setLastActiveTime(nowDate);
            updateTgClusterNode.setOnlineAccountCount(accounts.size());
            updateTgClusterNode.setTotalAccountCount(nodeAccountCount.intValue());
            tgClusterNodeService.update(updateTgClusterNode);
        }
    }

    /**
     * 获取公网IP地址。
     */
    private String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            log.warn("获取公网IP失败: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * 获取内网IP地址（eth0网卡）。
     */
    private String getPrivateIp() {
        try {
            NetworkInterface eth0 = NetworkInterface.getByName("eth0");
            if (eth0 != null && eth0.isUp()) {
                Enumeration<InetAddress> addresses = eth0.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取内网IP失败: {}", e.getMessage());
        }
        return "unknown";
    }

    private void initNodeInfo() {
        try {
            String nodeInfoFilePath = DATA_DIR + File.separator + "nodeinfo.txt";
            File nodeInfoFile = new File(nodeInfoFilePath);
            if (nodeInfoFile.exists()) {
                Properties properties = new Properties();
                FileInputStream fileInputStream = new FileInputStream(nodeInfoFilePath);
                properties.load(fileInputStream);
                fileInputStream.close();
                NODE_ID = properties.getProperty("ID");
                if (NODE_ID == null || NODE_ID.trim().length() <= 0) {
                    String nodeId = MD5.MD5generator16Bit(UUID.randomUUID().toString());
                    properties = new Properties();
                    properties.setProperty("ID", nodeId);
                    properties.store(new FileOutputStream(nodeInfoFilePath), "");
                    NODE_ID = nodeId;
                }
            } else {
                String nodeId = MD5.MD5generator16Bit(UUID.randomUUID().toString());
                Properties properties = new Properties();
                properties.setProperty("ID", nodeId);
                properties.store(new FileOutputStream(nodeInfoFilePath), "");
                NODE_ID = nodeId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("节点ID: {}", NODE_ID);
        log.info("===================================================");
    }



    /**
     * 登录账号（通过请求参数）。
     */
    public SessionInfo login(AccountLoginRequest request) {
        String name = request.getSessionName();
        if (accounts.containsKey(name)) {
            throw new IllegalStateException("Account '" + name + "' is already connected");
        }

        TelegramAccount account = new TelegramAccount(
                name,
                DATA_DIR,
                request.getProxy(),
                request.getDevice(),
                request.isAutoReadMessages(),
                request.getApiId(),
                request.getApiHash(),
                this
        );

        SessionInfo info = account.login(request.getSessionData());
        accounts.put(name, account);
        log.info("Account '{}' logged in, total active: {}", name, accounts.size());
        return info;
    }

    /**
     * 从数据库加载账号并登录。
     * 根据数据库记录的 session_content、代理信息、设备信息自动创建 TelegramAccount 并登录。
     */
    public SessionInfo loginFromDb(TgTelethonAccount dbAccount) {
        String name = dbAccount.getPhone();
        if (accounts.containsKey(name)) {
            throw new IllegalStateException("Account '" + name + "' is already connected");
        }

        if (dbAccount.getSessionContent() == null || dbAccount.getSessionContent().length == 0) {
            throw new IllegalArgumentException("Account '" + name + "' has no session_content in database");
        }

        // 从数据库记录构建代理信息
        ProxyInfo proxyInfo = null;
        if (dbAccount.getProxyHost() != null && !dbAccount.getProxyHost().isBlank()
                && dbAccount.getProxyPort() != null && dbAccount.getProxyPort() > 0) {
            proxyInfo = ProxyInfo.builder()
                    .host(dbAccount.getProxyHost())
                    .port(dbAccount.getProxyPort())
                    .username(dbAccount.getProxyUsername())
                    .password(dbAccount.getProxyPassword())
                    .build();
        }

        // 从数据库记录构建设备信息
        DeviceInfo deviceInfo = null;
        if (dbAccount.getDeviceModel() != null || dbAccount.getSystemVersion() != null
                || dbAccount.getAppVersion() != null) {
            deviceInfo = DeviceInfo.builder()
                    .deviceModel(dbAccount.getDeviceModel())
                    .systemVersion(dbAccount.getSystemVersion())
                    .appVersion(dbAccount.getAppVersion())
                    .langCode(dbAccount.getLangCode())
                    .systemLangCode(dbAccount.getSystemLangCode())
                    .build();
        }

        TelegramAccount account = new TelegramAccount(
                name,
                DATA_DIR,
                proxyInfo,
                deviceInfo,
                false,
                dbAccount.getApiId(),
                dbAccount.getApiHash(),
                this
        );

        SessionInfo info = account.login(dbAccount.getSessionContent());
        accounts.put(name, account);

        // 登录成功后更新数据库中的账号信息
        try {
            String nickname = "";
            if (info.getFirstName() != null) {
                nickname = info.getFirstName();
            }
            if (info.getLastName() != null && !info.getLastName().isBlank()) {
                nickname = nickname + " " + info.getLastName();
            }
            accountService.updateAfterLogin(
                    dbAccount.getId(),
                    info.getUserId(),
                    nickname.trim(),
                    info.getUsername(),
                    info.getPhone()
            );
        } catch (Exception e) {
            log.warn("Failed to update DB after login for '{}': {}", name, e.getMessage());
        }

        log.info("Account '{}' logged in from DB, total active: {}", name, accounts.size());
        return info;
    }

    /**
     * 根据名称获取账号对象。
     */
    public TelegramAccount getAccount(String sessionName) {
        return accounts.get(sessionName);
    }

    /**
     * 获取所有在线账号列表。
     */
    public List<SessionInfo> listAccounts() {
        List<SessionInfo> list = new ArrayList<>();
        for (TelegramAccount account : accounts.values()) {
            SessionInfo info = account.getSessionInfo();
            if (info != null) {
                list.add(info);
            } else {
                list.add(SessionInfo.builder()
                        .sessionName(account.getSessionName())
                        .connected(account.isConnected())
                        .build());
            }
        }
        return list;
    }

    /**
     * 断开指定账号的连接。
     */
    public boolean disconnect(String sessionName) {
        TelegramAccount account = accounts.remove(sessionName);
        if (account == null) {
            return false;
        }
        account.disconnect();

        // 更新数据库状态为 offline
        try {
            TgTelethonAccount dbAccount = accountService.getByPhone(sessionName);
            if (dbAccount != null) {
                accountService.updateStatus(dbAccount.getId(), "offline");
            }
        } catch (Exception e) {
            log.warn("Failed to update DB status after disconnect for '{}': {}", sessionName, e.getMessage());
        }

        log.info("Account '{}' disconnected, total active: {}", sessionName, accounts.size());
        return true;
    }

    /**
     * 断开所有账号的连接（应用关闭时调用）。
     */
    @PreDestroy
    public void disconnectAll() {
        log.info("Disconnecting all accounts ({})...", accounts.size());
        for (String name : new ArrayList<>(accounts.keySet())) {
            disconnect(name);
        }
    }

    /**
     * 发送纯文本消息。
     */
    public Map<String, Object> sendTextMessage(String sessionName, String chatId, String text) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendTextMessage(chatId, text);
    }

    /**
     * 发送图片消息（通过URL）。
     */
    public Map<String, Object> sendImageMessage(String sessionName, String chatId, String imageUrl) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendImageMessage(chatId, imageUrl);
    }

    /**
     * 发送图片消息（通过字节数组）。
     */
    public Map<String, Object> sendImageMessage(String sessionName, String chatId,
                                                 byte[] imageData, String fileName) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendImageMessage(chatId, imageData, fileName);
    }

    /**
     * 发送 文本+图片(caption) 消息（通过URL）。
     */
    public Map<String, Object> sendCaptionMessage(String sessionName, String chatId,
                                                   String caption, String imageUrl) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendCaptionMessage(chatId, caption, imageUrl);
    }

    /**
     * 发送 文本+图片(caption) 消息（通过上传字节数组）。
     */
    public Map<String, Object> sendCaptionMessage(String sessionName, String chatId,
                                                   String caption, byte[] imageData, String fileName) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.sendCaptionMessage(chatId, caption, imageData, fileName);
    }

    /**
     * 添加好友（普通模式，通过用户名或user_id）。
     */
    public Map<String, Object> addContact(String sessionName, String targetUserId,
                                           String firstName, String lastName, String phone) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.addContact(targetUserId, firstName, lastName, phone);
    }

    /**
     * 批量导入通讯录方式添加好友。
     */
    public Map<String, Object> importContacts(String sessionName, List<Map<String, String>> contacts) {
        TelegramAccount account = getAccountOrThrow(sessionName);
        return account.importContacts(contacts);
    }

    // ==================== TelegramEventListener 回调实现 ====================

    @Override
    public void onMessage(TelegramAccount account, TelegramMessage message) {
        String sessionName = account.getSessionName();
        log.info("[统一消息处理] 账号: {}, 消息ID: {}, 聊天: {}({}), 发送者: {}({}), 类型: {}, 内容: {}",
                sessionName, message.getMessageId(),
                message.getChatName(), message.getChatId(),
                message.getSenderUsername(), message.getSenderId(),
                message.getMessageType(), message.getText());

        // 更新数据库消息计数
        try {
            TgTelethonAccount dbAccount = accountService.getByPhone(sessionName);
            if (dbAccount != null) {
                accountService.incrementMsgCount(dbAccount.getId(), message.isOutgoing());
            }
        } catch (Exception e) {
            log.warn("[统一消息处理] 更新消息计数失败: account={}, error={}", sessionName, e.getMessage());
        }

        // TODO: 在此处扩展更多统一处理逻辑，如：
        // - 自动回复
        // - 消息转发
        // - 消息持久化存储
        // - WebSocket 推送到前端
    }

    @Override
    public void onDisconnect(TelegramAccount account, String reason) {
        String sessionName = account.getSessionName();
        log.warn("[统一断连处理] 账号: {}, 原因: {}", sessionName, reason);

        // 从活跃账号列表中移除
        accounts.remove(sessionName);

        // 更新数据库状态
        try {
            TgTelethonAccount dbAccount = accountService.getByPhone(sessionName);
            if (dbAccount != null) {
                // 根据断连原因设置不同状态
                String status = reason.contains("banned") || reason.contains("BANNED")
                        ? "banned" : "offline";
                accountService.updateStatus(dbAccount.getId(), status);
            }
        } catch (Exception e) {
            log.warn("[统一断连处理] 更新数据库状态失败: account={}, error={}", sessionName, e.getMessage());
        }

        log.info("[统一断连处理] 账号 '{}' 已从活跃列表移除, 剩余活跃: {}", sessionName, accounts.size());

        // TODO: 在此处扩展更多断连处理逻辑，如：
        // - 自动重连
        // - 告警通知
        // - WebSocket 推送断连事件到前端
    }

    // ==================== 私有方法 ====================

    private TelegramAccount getAccountOrThrow(String sessionName) {
        TelegramAccount account = accounts.get(sessionName);
        if (account == null) {
            throw new IllegalStateException("Account '" + sessionName + "' is not connected");
        }
        return account;
    }
}
