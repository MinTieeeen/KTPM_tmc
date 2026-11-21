package com.tmc.gate.access.domain;

import java.util.Optional;

/** Chiều của lượt được chấp nhận gần nhất của nhân viên tại nhà máy này (INV-04). */
public interface PresenceReader {

    Optional<Direction> lastAcceptedDirection(String employeeCode);
}
