package com.tmc.gate.sync.application;

import com.tmc.contract.heartbeat.Heartbeat;
import com.tmc.gate.access.domain.GateIdentity;
import com.tmc.gate.access.domain.StationConfig;
import com.tmc.gate.access.domain.StationConfigReader;
import com.tmc.gate.access.domain.TurnstileHealthReader;
import com.tmc.gate.sync.domain.OutboxStore;
import com.tmc.gate.sync.domain.SyncClient;
import com.tmc.gate.sync.domain.SyncStateStore;
import com.tmc.gate.sync.domain.SyncUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gửi heartbeat mỗi {@code heartbeat-interval} (FR-05, FR-06): {@code gateId}, {@code sentAt}, độ sâu outbox,
 * {@code lastSyncAt}, sức khỏe cổng xoay của mọi trạm. Thành công → cập nhật {@code last_success_at}.
 */
@Component
public class HeartbeatSender {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatSender.class);

    private final SyncClient client;
    private final OutboxStore outbox;
    private final SyncStateStore state;
    private final StationConfigReader stations;
    private final TurnstileHealthReader turnstileHealth;
    private final GateIdentity identity;
    private final Clock clock;

    public HeartbeatSender(SyncClient client, OutboxStore outbox, SyncStateStore state, StationConfigReader stations,
                           TurnstileHealthReader turnstileHealth, GateIdentity identity, Clock clock) {
        this.client = client;
        this.outbox = outbox;
        this.state = state;
        this.stations = stations;
        this.turnstileHealth = turnstileHealth;
        this.identity = identity;
        this.clock = clock;
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${tmc.gate.sync.heartbeat-interval:10s}")
    public void scheduledHeartbeat() {
        try {
            sendOnce();
        } catch (RuntimeException e) {
            log.error("Lỗi không mong đợi khi gửi heartbeat", e);
        }
    }

    public boolean sendOnce() {
        Map<String, String> turnstiles = new LinkedHashMap<>();
        for (StationConfig st : stations.findAll()) {
            turnstiles.put(st.stationId(), turnstileHealth.health(st.turnstileId()).name());
        }
        Heartbeat heartbeat = new Heartbeat(identity.gateId(), clock.instant(), outbox.countPending(),
                state.lastSuccessfulSyncAt().orElse(null), turnstiles);
        try {
            client.heartbeat(heartbeat);
        } catch (SyncUnavailableException e) {
            log.debug("Chưa gửi được heartbeat: {}", e.getMessage());
            return false;
        }
        Instant now = clock.instant();
        state.recordSuccessfulSync(now);
        return true;
    }
}
