package com.tmc.gate.localstore.infrastructure;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Chuyển {@link Instant} ↔ chuỗi ISO-8601 UTC có độ dài cố định (6 chữ số thập phân) để phép so sánh chuỗi
 * trong SQL ({@code <}, {@code >=}) đúng với thứ tự thời gian. {@code Instant.toString()} không dùng được vì
 * số chữ số thập phân thay đổi.
 */
final class SqliteTime {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS'Z'").withZone(ZoneOffset.UTC);

    private SqliteTime() {
    }

    static String format(Instant instant) {
        return instant == null ? null : FORMAT.format(instant.truncatedTo(ChronoUnit.MICROS));
    }

    static Instant parse(String value) {
        return value == null ? null : Instant.parse(value);
    }
}
