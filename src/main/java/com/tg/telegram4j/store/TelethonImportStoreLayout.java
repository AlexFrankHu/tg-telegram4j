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
 * Custom {@link FileStoreLayout} that can bootstrap from a Telethon .session file's
 * auth_key. On {@link #initialize()}, if the backing t4j.bin has no existing session,
 * the imported auth_key is injected so telegram4j skips the auth flow entirely.
 *
 * <p>After the first successful connection, the session is persisted to t4j.bin
 * by the parent class, so subsequent startups no longer need the .session file.
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
     * Allow persistence even when selfId is not yet known (it will be 0
     * on the very first import from a .session file). The parent class
     * normally requires selfId != 0 before saving.
     */
    @Override
    protected boolean isAssociatedToUser() {
        return super.isAssociatedToUser() || !authKeys.isEmpty();
    }

    /**
     * Manually set the self user id after a successful connection.
     * This enables proper session persistence to t4j.bin.
     */
    public void updateSelfId(long id) {
        this.selfId = id;
    }
}
