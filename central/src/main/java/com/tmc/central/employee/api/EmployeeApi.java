package com.tmc.central.employee.api;

import java.util.List;

/** Dữ liệu tham chiếu của C2 cho module khác (C1 bootstrap bản sao của gate – FR-10, DP-15). */
public interface EmployeeApi {

    /** Credential mới nhất của mỗi nhân viên kèm {@code employeeActive}. */
    List<CredentialState> allCredentialStates();

    /** Mọi khóa công khai ký QR đã lưu. */
    List<PublicKeyInfo> publicKeys();
}
