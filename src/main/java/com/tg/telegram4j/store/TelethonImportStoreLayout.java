package com.tg.telegram4j.store;

import io.netty.buffer.Unpooled;
import reactor.core.publisher.Mono;
import telegram4j.mtproto.auth.AuthKey;
import telegram4j.mtproto.store.FileStoreLayout;
import telegram4j.mtproto.store.StoreLayout;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 自定义的 {@link FileStoreLayout}，可以从 Telethon .session 文件的
 * auth_key 启动。在 {@link #initialize()} 时，如果 t4j.bin 中没有已有会话，
 * 则注入导入的 auth_key，使 telegram4j 跳过认证流程直接登录。
 *
 * <p>首次成功连接后，会话会由父类自动持久化到 t4j.bin，
 * 后续启动不再需要 .session 文件。
 */
public class TelethonImportStoreLayout extends FileStoreLayout {

    private final int importDcId;
    private final byte[] importAuthKeyBytes;

    public TelethonImportStoreLayout(StoreLayout entityDelegate,
                                     Path dataFile,
                                     int importDcId,
                                     byte[] importAuthKeyBytes) {
        super(entityDelegate, dataFile, Executors.newSingleThreadExecutor());
        this.importDcId = importDcId;
        this.importAuthKeyBytes = importAuthKeyBytes;
    }

    @Override
    public Mono<Void> initialize() {
        return super.initialize().then(Mono.fromRunnable(() -> {
            if (authKeys.isEmpty() && importAuthKeyBytes != null) {
                AuthKey authKey = new AuthKey(Unpooled.wrappedBuffer(importAuthKeyBytes));
                authKeys.put(importDcId, authKey);
                mainDcId = importDcId;
            }
        }));
    }

    /**
     * 即使 selfId 还未知也允许持久化（首次从 .session 文件导入时 selfId 为 0）。
     * 父类通常要求 selfId != 0 才保存。
     */
    @Override
    protected boolean isAssociatedToUser() {
        return super.isAssociatedToUser() || !authKeys.isEmpty();
    }

    /**
     * 连接成功后手动设置自身用户ID。
     * 这样才能正确地将会话持久化到 t4j.bin。
     */
    public void updateSelfId(long id) {
        this.selfId = id;
    }
}
