package com.tmc.central.ingestion.domain;

/** Loại dữ liệu tham chiếu trong {@code ref_changes}. */
public enum RefChangeKind {
    /** key = credentialId, payload = {@code CredentialEntryDto}. */
    CREDENTIAL,
    /** key = kid, payload = {@code PublicKeyDto}. */
    PUBLIC_KEY,
    /** key = stationId, payload = {@code StationConfigDto}. */
    STATION
}
