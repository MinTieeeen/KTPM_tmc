package com.tmc.central.iam.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Ánh xạ API key → {@code gateId} (thay mTLS, độ lệch D2). Cấu hình {@code tmc.central.sync.gate-api-keys} dạng
 * {@code gateId=key,gateId=key}. So khớp bằng {@code MessageDigest.isEqual}, duyệt hết danh sách (NFR-02).
 * Không log giá trị key.
 */
@Component
public class GateApiKeyRegistry {

    private final Map<String, byte[]> keysByGate;

    public GateApiKeyRegistry(@Value("${tmc.central.sync.gate-api-keys:}") String config) {
        Map<String, byte[]> parsed = new LinkedHashMap<>();
        for (String entry : config.split(",")) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int eq = trimmed.indexOf('=');
            if (eq <= 0 || eq == trimmed.length() - 1) {
                throw new IllegalStateException("Cấu hình gate-api-keys sai định dạng (cần gateId=key)");
            }
            parsed.put(trimmed.substring(0, eq).trim(), trimmed.substring(eq + 1).trim().getBytes(StandardCharsets.UTF_8));
        }
        this.keysByGate = Map.copyOf(parsed);
    }

    /** {@code gateId} ứng với key, hoặc rỗng nếu key thiếu/sai. */
    public Optional<String> resolve(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }
        byte[] candidate = apiKey.getBytes(StandardCharsets.UTF_8);
        String match = null;
        for (Map.Entry<String, byte[]> e : keysByGate.entrySet()) {
            if (MessageDigest.isEqual(e.getValue(), candidate)) {
                match = e.getKey();
            }
        }
        return Optional.ofNullable(match);
    }

    public Set<String> gateIds() {
        return keysByGate.keySet();
    }
}
