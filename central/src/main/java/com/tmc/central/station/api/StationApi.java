package com.tmc.central.station.api;

import java.util.List;
import java.util.Optional;

/**
 * Dữ liệu trạm và gate cho C1 (FR-02, FR-03). C1 gọi trực tiếp interface này thay vì phát sự kiện, để không có
 * vòng phụ thuộc C1 ↔ C3.
 */
public interface StationApi {

    Optional<StationInfo> findStation(String stationId);

    Optional<GateInfo> findGate(String gateId);

    List<StationInfo> allStations();

    /**
     * Lưu heartbeat mới nhất của gate; heartbeat có {@code sentAt} cũ hơn bản đang lưu bị bỏ qua.
     *
     * @throws UnknownGateException gate chưa đăng ký
     */
    void recordHeartbeat(HeartbeatRecord record);
}
