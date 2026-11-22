package com.tmc.gate.localstore.infrastructure;

import com.tmc.gate.access.domain.Direction;
import com.tmc.gate.access.domain.StationConfig;
import com.tmc.gate.access.domain.StationConfigReader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Đọc cấu hình trạm cục bộ (bảng {@code stations}, do G4 ghi qua {@link SqliteCredentialStore#applyDelta}). */
@Repository
public class SqliteStationStore implements StationConfigReader {

    private static final RowMapper<StationConfig> MAPPER = (rs, rowNum) -> new StationConfig(
            rs.getString("station_id"),
            rs.getString("factory_id"),
            Direction.valueOf(rs.getString("direction")),
            rs.getInt("enabled") == 1,
            rs.getString("turnstile_id"));

    private final JdbcClient jdbc;

    public SqliteStationStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<StationConfig> find(String stationId) {
        return jdbc.sql("SELECT * FROM stations WHERE station_id = :id")
                .param("id", stationId)
                .query(MAPPER)
                .optional();
    }

    @Override
    public List<StationConfig> findAll() {
        return jdbc.sql("SELECT * FROM stations ORDER BY station_id")
                .query(MAPPER)
                .list();
    }
}
