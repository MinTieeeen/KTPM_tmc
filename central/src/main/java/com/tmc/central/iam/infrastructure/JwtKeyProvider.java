package com.tmc.central.iam.infrastructure;

import com.tmc.central.iam.application.IamProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Cặp khóa RSA ký JWT (RS256). Đọc từ file PEM ({@code JWT_PRIVATE_KEY_PATH}, {@code JWT_PUBLIC_KEY_PATH});
 * profile demo ({@code tmc.central.demo.auto-generate-keys=true}) tự sinh cặp khóa 2048 bit khi để trống – khi đó
 * token cũ mất hiệu lực sau mỗi lần khởi động lại (độ lệch D3). Thiếu khóa ngoài demo → không khởi động.
 * Không log nội dung khóa.
 */
@Component
public class JwtKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyProvider.class);

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public JwtKeyProvider(IamProperties props,
                          @Value("${tmc.central.demo.auto-generate-keys:false}") boolean autoGenerate) {
        String privatePath = props.jwt().privateKeyPath();
        String publicPath = props.jwt().publicKeyPath();
        try {
            if (!privatePath.isBlank() && !publicPath.isBlank()) {
                KeyFactory rsa = KeyFactory.getInstance("RSA");
                this.privateKey = (RSAPrivateKey) rsa.generatePrivate(new PKCS8EncodedKeySpec(readPem(privatePath)));
                this.publicKey = (RSAPublicKey) rsa.generatePublic(new X509EncodedKeySpec(readPem(publicPath)));
                log.info("Đã nạp khóa RSA ký JWT từ file PEM");
            } else if (autoGenerate) {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair pair = generator.generateKeyPair();
                this.privateKey = (RSAPrivateKey) pair.getPrivate();
                this.publicKey = (RSAPublicKey) pair.getPublic();
                log.warn("Chưa cấu hình khóa JWT – đã tự sinh cặp khóa RSA (chỉ dùng cho demo)");
            } else {
                throw new IllegalStateException(
                        "Thiếu khóa ký JWT: đặt JWT_PRIVATE_KEY_PATH và JWT_PUBLIC_KEY_PATH, hoặc bật tmc.central.demo.auto-generate-keys");
            }
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Không đọc được khóa ký JWT", e);
        }
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }

    RSAPrivateKey privateKey() {
        return privateKey;
    }

    private static byte[] readPem(String path) throws IOException {
        String pem = Files.readString(Path.of(path));
        String base64 = pem.replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
