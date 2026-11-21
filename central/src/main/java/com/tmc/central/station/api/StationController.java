package com.tmc.central.station.api;

import com.tmc.central.station.application.ListStationsQuery;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** {@code GET /stations} – tab Trạm của Admin, làm mới mỗi 5 s (FR-04). */
@RestController
public class StationController {

    private final ListStationsQuery listStations;

    public StationController(ListStationsQuery listStations) {
        this.listStations = listStations;
    }

    @GetMapping("/stations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StationView> stations() {
        return listStations.list();
    }
}
