package com.tmc.gate.access.domain;

import java.util.List;
import java.util.Optional;

/** Đọc cấu hình trạm cục bộ (INV-03). */
public interface StationConfigReader {

    Optional<StationConfig> find(String stationId);

    List<StationConfig> findAll();
}
