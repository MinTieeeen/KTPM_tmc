package com.tmc.central.ingestion.domain;

import java.time.Instant;
import java.util.List;

/** Nhật ký thay đổi dữ liệu tham chiếu (DP-04: C1 không đọc bảng của C2/C3). */
public interface RefChangeStore {

    /** Thêm một dòng với {@code version} mới; trả version đã cấp. */
    long append(RefChangeKind kind, String key, String payloadJson, Instant changedAt);

    /** Bản mới nhất theo {@code (kind, key)} của các dòng có {@code version > since}. */
    List<RefChange> latestSince(long since);

    /** Version lớn nhất hiện có; {@code 0} khi bảng rỗng. */
    long currentVersion();

    long countByKind(RefChangeKind kind);
}
