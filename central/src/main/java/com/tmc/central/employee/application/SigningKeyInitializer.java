package com.tmc.central.employee.application;

import com.tmc.central.employee.api.SigningKeyPublished;
import com.tmc.central.employee.domain.QrTokenSigner;
import com.tmc.central.employee.domain.SigningKey;
import com.tmc.central.employee.domain.SigningKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

/**
 * Khi khởi động: lưu khóa công khai đang dùng vào {@code signing_keys}, đặt khóa khác {@code active=false} và
 * phát {@link SigningKeyPublished} cho mỗi thay đổi (FR-08). Không phát gì nếu khóa không đổi so với lần trước.
 */
@Component
public class SigningKeyInitializer {

    private static final Logger log = LoggerFactory.getLogger(SigningKeyInitializer.class);

    private final QrTokenSigner signer;
    private final SigningKeyRepository keys;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public SigningKeyInitializer(QrTokenSigner signer, SigningKeyRepository keys, ApplicationEventPublisher events,
                                 Clock clock) {
        this.signer = signer;
        this.keys = keys;
        this.events = events;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void publishCurrentKey() {
        Instant now = clock.instant();
        String kid = signer.keyId();
        byte[] publicKey = signer.publicKeyX509();

        for (SigningKey other : keys.findAll()) {
            if (!other.kid().equals(kid) && other.active()) {
                other.deactivate();
                keys.save(other);
                publish(other, now);
            }
        }

        SigningKey current = keys.findByKid(kid).orElse(null);
        if (current == null) {
            current = keys.save(SigningKey.publish(kid, publicKey, now));
            publish(current, now);
        } else if (current.differsFrom(publicKey)) {
            current.replace(publicKey, now);
            current = keys.save(current);
            publish(current, now);
        } else {
            log.info("Khóa ký QR kid={} không đổi", kid);
        }
    }

    private void publish(SigningKey key, Instant now) {
        events.publishEvent(new SigningKeyPublished(key.kid(),
                Base64.getEncoder().encodeToString(key.publicKey()), key.active(), now));
        log.info("Đã công bố khóa ký QR kid={} active={}", key.kid(), key.active());
    }
}
