package com.tmc.central.iam.domain;

/**
 * Đăng nhập thất bại. Dùng chung cho mọi lý do (sai tên, sai mật khẩu, tài khoản bị vô hiệu hóa) để không
 * phân biệt được từ bên ngoài (NFR-SEC-04).
 */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException() {
        super("Invalid credentials");
    }
}
