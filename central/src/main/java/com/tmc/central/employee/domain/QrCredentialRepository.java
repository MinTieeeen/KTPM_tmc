package com.tmc.central.employee.domain;

import java.util.List;
import java.util.Optional;

/** Kho credential (cài đặt: JpaQrCredentialRepository). */
public interface QrCredentialRepository {

    Optional<QrCredential> findByEmployeeCodeAndStatus(String employeeCode, CredentialStatus status);

    /** Credential mới nhất của nhân viên (theo {@code issuedAt}). */
    Optional<QrCredential> findFirstByEmployeeCodeOrderByIssuedAtDesc(String employeeCode);

    List<QrCredential> findAll();

    QrCredential save(QrCredential credential);

    /**
     * Đẩy thay đổi xuống CSDL ngay. Cần khi cấp lại: phải thu hồi credential cũ trước khi chèn credential mới,
     * vì Hibernate mặc định chạy INSERT trước UPDATE và sẽ vi phạm {@code ux_one_active}.
     */
    void flush();
}
