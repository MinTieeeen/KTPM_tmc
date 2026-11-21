package com.tmc.gate.access.application;

import com.tmc.gate.access.domain.AccessRules;
import com.tmc.gate.access.domain.AttendanceTransaction;
import com.tmc.gate.access.domain.CommandStatus;
import com.tmc.gate.access.domain.ConnectivityState;
import com.tmc.gate.access.domain.CredentialEntry;
import com.tmc.gate.access.domain.CredentialReader;
import com.tmc.gate.access.domain.Decision;
import com.tmc.gate.access.domain.Direction;
import com.tmc.gate.access.domain.GateResult;
import com.tmc.gate.access.domain.NonceAlreadyUsedException;
import com.tmc.gate.access.domain.NonceRegistry;
import com.tmc.gate.access.domain.OpenCommand;
import com.tmc.gate.access.domain.PresenceReader;
import com.tmc.gate.access.domain.QrTokenVerifier;
import com.tmc.gate.access.domain.RejectReason;
import com.tmc.gate.access.domain.ScanAttempt;
import com.tmc.gate.access.domain.ScanAttemptStore;
import com.tmc.gate.access.domain.ScanContext;
import com.tmc.gate.access.domain.ScanStatus;
import com.tmc.gate.access.domain.StationConfig;
import com.tmc.gate.access.domain.StationConfigReader;
import com.tmc.gate.access.domain.TransactionStore;
import com.tmc.gate.access.domain.TurnstileGateway;
import com.tmc.gate.access.domain.VerificationResult;
import com.tmc.gate.access.domain.VerifiedToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Điều phối luồng F1 – quét mã QR (mục 2.3 đặc tả M2, mục 6.4 spec). Là nơi duy nhất quyết định trả kết quả gì
 * (SRP); chỉ phụ thuộc port (DIP).
 * <p>Thứ tự bất biến: lưu PENDING (TX #1) → gọi cổng xoay (ngoài transaction) → chốt ACCEPTED (TX #2) rồi mới
 * trả xanh (INV-05). Không có lời gọi nào tới Central (DP-05).
 */
@Service
public class ProcessScanUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessScanUseCase.class);

    private final QrTokenVerifier verifier;
    private final StationConfigReader stations;
    private final CredentialReader credentials;
    private final NonceRegistry nonces;
    private final PresenceReader presence;
    private final ConnectivityState connectivity;
    private final AccessRules rules;
    private final ScanAttemptStore scans;
    private final TransactionStore transactions;
    private final TurnstileGateway turnstile;
    private final AccessSettings settings;
    private final Clock clock;

    public ProcessScanUseCase(QrTokenVerifier verifier, StationConfigReader stations, CredentialReader credentials,
                              NonceRegistry nonces, PresenceReader presence, ConnectivityState connectivity,
                              AccessRules rules, ScanAttemptStore scans, TransactionStore transactions,
                              TurnstileGateway turnstile, AccessSettings settings, Clock clock) {
        this.verifier = verifier;
        this.stations = stations;
        this.credentials = credentials;
        this.nonces = nonces;
        this.presence = presence;
        this.connectivity = connectivity;
        this.rules = rules;
        this.scans = scans;
        this.transactions = transactions;
        this.turnstile = turnstile;
        this.settings = settings;
        this.clock = clock;
    }

    public ScanOutcome handle(ScanCommand cmd) {
        Instant now = clock.instant();
        UUID eventId = UUID.randomUUID();
        Optional<StationConfig> station = stations.find(cmd.stationId());
        Direction stationDirection = station.map(StationConfig::direction).orElse(null);

        // 1, 2. Định dạng, kid, chữ ký, thời hạn
        VerificationResult verification = verifier.verify(cmd.raw(), now);
        if (verification instanceof VerificationResult.Fail fail) {
            return reject(cmd, eventId, null, fail.reason(), stationDirection, now);
        }
        VerifiedToken token = ((VerificationResult.Ok) verification).token();

        // 3 → 7. Quy tắc tại cổng trên dữ liệu cục bộ
        Optional<CredentialEntry> credential = credentials.findById(token.credentialId());
        ScanContext ctx = new ScanContext(
                token,
                station,
                credential,
                nonces.isUsed(token.nonce()),
                credential.flatMap(c -> presence.lastAcceptedDirection(c.employeeCode())),
                connectivity.lastSuccessfulSyncAt(),
                Duration.ofMinutes(connectivity.offlineThresholdMinutes()),
                now);

        Decision decision = rules.evaluate(ctx);
        if (decision instanceof Decision.Deny deny) {
            return reject(cmd, eventId, token.credentialId(), deny.reason(), stationDirection, now);
        }
        Decision.Allow allow = (Decision.Allow) decision;
        StationConfig st = station.orElseThrow(); // AccessRules đã bảo đảm trạm tồn tại
        if (allow.warning() != null) {
            log.warn("Lượt quét {} của {} tại {}: {}", eventId, allow.employeeCode(), st.stationId(), allow.warning());
        }

        // TX #1: lưu PENDING + đánh dấu nonce trước khi gọi cổng (INV-05, DP-01)
        ScanAttempt attempt = ScanAttempt.pending(eventId, st.stationId(), token.credentialId(), now);
        try {
            scans.recordPending(attempt, token.nonce(), token.expiresAt().plus(settings.clockSkew()));
        } catch (NonceAlreadyUsedException e) {
            // Hai yêu cầu đồng thời cùng token: yêu cầu thua bị từ chối
            return reject(cmd, eventId, token.credentialId(), RejectReason.NONCE_REUSED, stationDirection, now);
        }

        // Gọi cổng xoay – ngoài mọi transaction (SQLite chỉ có một kết nối)
        GateResult result = turnstile.open(new OpenCommand(st.turnstileId(), eventId, st.stationId()),
                settings.openTimeout());
        if (result == GateResult.TIMEOUT || result == GateResult.ERROR) {
            // Kiểm tra trạng thái một lần trước khi kết luận thất bại (NFR-REL-02); không tự gửi lại lệnh mở
            CommandStatus status = turnstile.status(st.turnstileId(), eventId, settings.statusTimeout());
            if (status == CommandStatus.EXECUTED) {
                log.info("Lượt quét {}: cổng {} trả {} nhưng đã thực hiện lệnh", eventId, st.turnstileId(), result);
                result = GateResult.OPENED;
            }
        }

        if (result == GateResult.OPENED) {
            // TX #2: chốt ACCEPTED + giao dịch + presence + 2 outbox; lỗi ở đây → lan lên (HTTP 500), không trả xanh
            AttendanceTransaction tx = new AttendanceTransaction(UUID.randomUUID(), eventId, allow.employeeCode(),
                    st.stationId(), allow.direction(), clock.instant());
            transactions.confirmAccepted(eventId, tx);
            return outcome(cmd, eventId, ScanStatus.ACCEPTED, null, allow.employeeCode(), allow.direction(),
                    token.credentialId());
        }

        RejectReason gateReason = switch (result) {
            case TIMEOUT -> RejectReason.GATE_TIMEOUT;
            case ERROR -> RejectReason.GATE_ERROR;
            case CIRCUIT_OPEN -> RejectReason.CIRCUIT_OPEN;
            case OPENED -> throw new IllegalStateException("unreachable");
        };
        scans.markGateFailed(eventId, gateReason, clock.instant());
        return outcome(cmd, eventId, ScanStatus.GATE_FAILED, gateReason, allow.employeeCode(), allow.direction(),
                token.credentialId());
    }

    /** Lượt bị từ chối: một transaction {scan_attempt REJECTED + outbox} (INV-07). */
    private ScanOutcome reject(ScanCommand cmd, UUID eventId, UUID credentialId, RejectReason reason,
                               Direction direction, Instant now) {
        scans.recordRejected(ScanAttempt.rejected(eventId, cmd.stationId(), credentialId, reason, now));
        return outcome(cmd, eventId, ScanStatus.REJECTED, reason, null, direction, credentialId);
    }

    private ScanOutcome outcome(ScanCommand cmd, UUID eventId, ScanStatus result, RejectReason reason,
                                String employeeCode, Direction direction, UUID credentialId) {
        long elapsedMs = Math.max(0, clock.millis() - cmd.receivedAt().toEpochMilli());
        log.info("Lượt quét {} tại {} (cid={}): {}{} trong {} ms", eventId, cmd.stationId(), credentialId, result,
                reason == null ? "" : " " + reason, elapsedMs);
        return new ScanOutcome(result, reason, employeeCode, direction, elapsedMs, eventId);
    }
}
