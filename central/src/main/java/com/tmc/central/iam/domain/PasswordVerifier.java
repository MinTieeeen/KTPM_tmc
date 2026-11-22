package com.tmc.central.iam.domain;

/** So khớp mật khẩu với hash (cài đặt: BCryptPasswordVerifier). */
public interface PasswordVerifier {

    boolean matches(String rawPassword, String passwordHash);

    /**
     * So khớp với một hash giả khi tên đăng nhập không tồn tại, để thời gian phản hồi tương đương trường hợp
     * sai mật khẩu (chống dò tên tài khoản – FR-02).
     */
    void dummyCheck(String rawPassword);
}
