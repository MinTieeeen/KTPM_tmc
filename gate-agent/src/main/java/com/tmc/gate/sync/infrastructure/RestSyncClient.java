package com.tmc.gate.sync.infrastructure;

import com.tmc.contract.SyncPaths;
import com.tmc.contract.event.BatchAck;
import com.tmc.contract.event.EventBatch;
import com.tmc.contract.heartbeat.Heartbeat;
import com.tmc.contract.snapshot.SnapshotDelta;
import com.tmc.gate.sync.domain.NetworkFaultSwitch;
import com.tmc.gate.sync.domain.SyncClient;
import com.tmc.gate.sync.domain.SyncUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

/**
 * Gọi SyncAPI của Central bằng {@link RestClient}. Kiểm {@link NetworkFaultSwitch} trước mỗi lời gọi (FR-08); mọi lỗi
 * (mạng, timeout, 4xx, 5xx, body rỗng) → {@link SyncUnavailableException}. Không log API key hay payload (NFR-04).
 */
@Component
public class RestSyncClient implements SyncClient {

    private final RestClient rest;
    private final NetworkFaultSwitch faultSwitch;

    public RestSyncClient(@Qualifier(SyncClientConfig.CENTRAL_REST_CLIENT) RestClient rest,
                          NetworkFaultSwitch faultSwitch) {
        this.rest = rest;
        this.faultSwitch = faultSwitch;
    }

    @Override
    public BatchAck push(EventBatch batch) {
        return call("push", () -> rest.post()
                .uri(SyncPaths.EVENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .body(batch)
                .retrieve()
                .body(BatchAck.class));
    }

    @Override
    public SnapshotDelta pull(long sinceVersion) {
        return call("pull", () -> rest.get()
                .uri(uri -> uri.path(SyncPaths.SNAPSHOT).queryParam(SyncPaths.SNAPSHOT_SINCE_PARAM, sinceVersion).build())
                .retrieve()
                .body(SnapshotDelta.class));
    }

    @Override
    public void heartbeat(Heartbeat heartbeat) {
        call("heartbeat", () -> rest.post()
                .uri(SyncPaths.HEARTBEAT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(heartbeat)
                .retrieve()
                .toBodilessEntity());
    }

    private <T> T call(String operation, Supplier<T> request) {
        if (faultSwitch.isOffline()) {
            throw new SyncUnavailableException(operation + ": đang giả lập mất kết nối");
        }
        try {
            T result = request.get();
            if (result == null) {
                throw new SyncUnavailableException(operation + ": Central trả body rỗng");
            }
            return result;
        } catch (RestClientResponseException e) {
            throw new SyncUnavailableException(operation + ": Central trả HTTP " + e.getStatusCode().value(),
                    e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new SyncUnavailableException(operation + ": không kết nối được Central (" + e.getClass().getSimpleName() + ")",
                    0, e);
        }
    }
}
