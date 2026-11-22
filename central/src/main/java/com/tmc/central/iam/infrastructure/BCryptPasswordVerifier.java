package com.tmc.central.iam.infrastructure;

import com.tmc.central.iam.application.IamProperties;
import com.tmc.central.iam.domain.PasswordVerifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** So khớp mật khẩu BCrypt (cost theo cấu hình, mặc định 10). */
@Component
public class BCryptPasswordVerifier implements PasswordVerifier {

    private final BCryptPasswordEncoder encoder;
    /** Hash giả cùng cost để nhánh "tên không tồn tại" tốn thời gian như nhánh "sai mật khẩu". */
    private final String dummyHash;

    public BCryptPasswordVerifier(IamProperties props) {
        this.encoder = new BCryptPasswordEncoder(props.password().bcryptStrength());
        this.dummyHash = encoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        return encoder.matches(rawPassword, passwordHash);
    }

    @Override
    public void dummyCheck(String rawPassword) {
        encoder.matches(rawPassword == null ? "" : rawPassword, dummyHash);
    }
}
