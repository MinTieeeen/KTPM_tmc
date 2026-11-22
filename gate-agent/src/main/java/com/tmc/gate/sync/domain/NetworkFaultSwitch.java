package com.tmc.gate.sync.domain;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Công tắc giả lập mất kết nối Gate ↔ Central (D5): khi bật, mọi lời gọi Central ném
 * {@link SyncUnavailableException} mà không ra mạng. Dùng cho demo D4/T11 không cần quyền Docker.
 */
public final class NetworkFaultSwitch {

    private final AtomicBoolean offline = new AtomicBoolean(false);

    public void setOffline(boolean value) {
        offline.set(value);
    }

    public boolean isOffline() {
        return offline.get();
    }
}
