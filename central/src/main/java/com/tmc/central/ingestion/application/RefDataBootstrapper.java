package com.tmc.central.ingestion.application;

import com.tmc.central.employee.api.EmployeeApi;
import com.tmc.central.ingestion.domain.RefChangeKind;
import com.tmc.central.ingestion.domain.RefChangeStore;
import com.tmc.central.station.api.StationApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Khi khởi động: ghi trạng thái hiện tại vào {@code ref_changes} qua interface công khai của C2/C3 (FR-08, D2) –
 * dữ liệu seed của Flyway không đi qua sự kiện. Kiểm <b>theo từng loại</b> chứ không theo cả bảng: sự kiện
 * {@code SigningKeyPublished} phát lúc khởi động có thể ghi vào bảng trước khi lớp này chạy, và kiểm cả bảng sẽ
 * làm bỏ sót credential và trạm.
 */
@Component
public class RefDataBootstrapper {

    private static final Logger log = LoggerFactory.getLogger(RefDataBootstrapper.class);

    private final RefChangeStore store;
    private final RefChangeRecorder recorder;
    private final EmployeeApi employees;
    private final StationApi stations;
    private final Clock clock;

    public RefDataBootstrapper(RefChangeStore store, RefChangeRecorder recorder, EmployeeApi employees,
                               StationApi stations, Clock clock) {
        this.store = store;
        this.recorder = recorder;
        this.employees = employees;
        this.stations = stations;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrap() {
        Instant now = clock.instant();
        if (store.countByKind(RefChangeKind.CREDENTIAL) == 0) {
            var states = employees.allCredentialStates();
            states.forEach(s -> recorder.recordCredential(s, now));
            log.info("Bootstrap ref_changes: {} credential", states.size());
        }
        if (store.countByKind(RefChangeKind.PUBLIC_KEY) == 0) {
            var keys = employees.publicKeys();
            keys.forEach(k -> recorder.recordPublicKey(k, now));
            log.info("Bootstrap ref_changes: {} khóa công khai", keys.size());
        }
        if (store.countByKind(RefChangeKind.STATION) == 0) {
            var all = stations.allStations();
            all.forEach(s -> recorder.recordStation(s, now));
            log.info("Bootstrap ref_changes: {} trạm", all.size());
        }
    }
}
