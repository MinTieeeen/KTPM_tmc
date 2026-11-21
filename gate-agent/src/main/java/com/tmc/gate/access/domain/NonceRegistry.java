package com.tmc.gate.access.domain;

/**
 * Tra cứu nonce đã dùng. Nonce đã hết hạn và bị dọn trả {@code false} – an toàn vì token đó đã hết hạn.
 */
public interface NonceRegistry {

    boolean isUsed(String nonce);
}
