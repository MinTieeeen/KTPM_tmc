package com.tmc.gate.access.application;

import com.tmc.gate.access.domain.AccessRules;
import com.tmc.gate.access.domain.ConnectivityState;
import com.tmc.gate.access.domain.GateIdentity;
import com.tmc.gate.access.domain.OutboxMonitor;
import com.tmc.gate.access.domain.ScanAttemptStore;
import com.tmc.gate.access.domain.StationConfig;
import com.tmc.gate.access.domain.StationConfigReader;
import com.tmc.gate.access.domain.TurnstileHealth;
import com.tmc.gate.access.domain.TurnstileHealthReader;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Gom trạng thái kết nối, outbox, lượt PENDING bị kẹt và sức khỏe cổng xoay cho {@code GET /status}. */
@Service
public class GetGateStatusUseCase {

    private final GateIdentity identity;
    private final ConnectivityState connectivity;
    private final OutboxMonitor outbox;
    private final ScanAttemptStore scans;
    private final StationConfigReader stations;
    private final TurnstileHealthReader turnstileHealth;
    private final AccessSettings settings;
    private final Clock clock;

    public GetGateStatusUseCase(GateIdentity identity, ConnectivityState connectivity, OutboxMonitor outbox,
                                ScanAttemptStore scans, StationConfigReader stations,
                                TurnstileHealthReader turnstileHealth, AccessSettings settings, Clock clock) {
        this.identity = identity;
        this.connectivity = connectivity;
        this.outbox = outbox;
        this.scans = scans;
        this.stations = stations;
        this.turnstileHealth = turnstileHealth;
        this.settings = settings;
        this.clock = clock;
    }

    public GateStatusView get() {
        Instant now = clock.instant();
        Optional<Instant> lastSync = connectivity.lastSuccessfulSyncAt();
        boolean online = lastSync
                .map(last -> Duration.between(last, now).compareTo(settings.onlineWithin()) <= 0)
                .orElse(false);
        boolean thresholdExceeded = AccessRules.isOfflineThresholdExceeded(
                lastSync, Duration.ofMinutes(connectivity.offlineThresholdMinutes()), now);

        Instant stuckBefore = now.minus(settings.stuckPendingAfter());
        long pendingReconcile = scans.findPending().stream()
                .filter(a -> a.occurredAt().isBefore(stuckBefore))
                .count();

        Map<String, TurnstileHealth> turnstiles = new LinkedHashMap<>();
        for (StationConfig st : stations.findAll()) {
            turnstiles.put(st.stationId(), turnstileHealth.health(st.turnstileId()));
        }

        return new GateStatusView(identity.gateId(), online, lastSync.orElse(null), outbox.pendingCount(),
                thresholdExceeded, pendingReconcile, Map.copyOf(turnstiles));
    }
}
