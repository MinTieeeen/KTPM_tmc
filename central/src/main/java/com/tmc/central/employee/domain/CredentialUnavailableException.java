package com.tmc.central.employee.domain;

/** Không thể lấy/thao tác credential trong trạng thái hiện tại ({@code 409} kèm {@link #code()}). */
public class CredentialUnavailableException extends RuntimeException {

    /** Credential gần nhất đã bị thu hồi. */
    public static final String CREDENTIAL_REVOKED = "CREDENTIAL_REVOKED";
    /** Chưa từng được cấp, hoặc không có credential ACTIVE để thu hồi. */
    public static final String NO_ACTIVE_CREDENTIAL = "NO_ACTIVE_CREDENTIAL";
    /** Nhân viên không còn hoạt động. */
    public static final String EMPLOYEE_INACTIVE = "EMPLOYEE_INACTIVE";

    private final String code;

    public CredentialUnavailableException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
