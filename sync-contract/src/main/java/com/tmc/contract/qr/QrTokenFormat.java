package com.tmc.contract.qr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.tmc.contract.qr.QrTokenFormatException.Code;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

/**
 * Mã hóa / giải mã chuỗi QR động (mục 6.4, ADR-02):
 * <pre>raw = "TMC1." + base64url(payloadJson) + "." + base64url(ed25519Signature(payloadJson))</pre>
 * base64url không padding. Lớp này không ký và không kiểm chữ ký, thời hạn:
 * C2 ký trên đúng mảng byte trả về từ {@link #serializePayload}, G1 kiểm trên {@link QrTokenParts#payloadJson()}.
 */
public final class QrTokenFormat {

    public static final String PREFIX = "TMC1.";
    public static final int FORMAT_VERSION = 1;
    public static final int SIGNATURE_LENGTH = 64;
    /** Giới hạn độ dài chuỗi trước khi giải mã, chống chuỗi rác quá dài. */
    public static final int MAX_RAW_LENGTH = 1024;

    /** ObjectMapper bất biến sau khi khởi tạo, an toàn đa luồng. */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private static final Base64.Encoder B64URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64URL_DECODER = Base64.getUrlDecoder();

    private QrTokenFormat() {
    }

    /** Tuần tự hóa payload thành byte JSON (thứ tự trường cố định) – đây là dữ liệu được ký. */
    public static byte[] serializePayload(QrTokenPayload payload) {
        Objects.requireNonNull(payload, "payload");
        try {
            return MAPPER.writeValueAsBytes(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không tuần tự hóa được payload QR", e);
        }
    }

    /** Ghép chuỗi QR từ byte payload và chữ ký. */
    public static String encode(byte[] payloadJson, byte[] signature) {
        Objects.requireNonNull(payloadJson, "payloadJson");
        Objects.requireNonNull(signature, "signature");
        if (signature.length != SIGNATURE_LENGTH) {
            throw new IllegalArgumentException("Chữ ký Ed25519 phải dài " + SIGNATURE_LENGTH + " byte");
        }
        return PREFIX + B64URL_ENCODER.encodeToString(payloadJson) + "." + B64URL_ENCODER.encodeToString(signature);
    }

    /**
     * Tách và giải mã chuỗi QR. Không kiểm chữ ký, không kiểm thời hạn.
     *
     * @throws QrTokenFormatException {@code INVALID_FORMAT} hoặc {@code UNSUPPORTED_VERSION}
     */
    public static QrTokenParts parse(String raw) {
        if (raw == null || raw.length() > MAX_RAW_LENGTH || !raw.startsWith(PREFIX)) {
            throw invalid("Thiếu tiền tố " + PREFIX + " hoặc chuỗi quá dài", null);
        }
        String[] parts = raw.substring(PREFIX.length()).split("\\.", -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw invalid("Chuỗi QR phải gồm 2 phần payload và chữ ký", null);
        }

        byte[] payloadJson = decodeBase64Url(parts[0]);
        byte[] signature = decodeBase64Url(parts[1]);
        if (signature.length != SIGNATURE_LENGTH) {
            throw invalid("Chữ ký sai độ dài: " + signature.length, null);
        }

        JsonNode tree;
        try {
            tree = MAPPER.readTree(payloadJson);
        } catch (IOException e) {
            throw invalid("Payload không phải JSON hợp lệ", e);
        }
        if (tree == null || !tree.isObject() || !tree.path("v").isInt()) {
            throw invalid("Payload thiếu trường v", null);
        }
        int version = tree.get("v").intValue();
        if (version != FORMAT_VERSION) {
            throw new QrTokenFormatException(Code.UNSUPPORTED_VERSION, "Phiên bản QR không hỗ trợ: " + version);
        }

        QrTokenPayload payload;
        try {
            payload = MAPPER.treeToValue(tree, QrTokenPayload.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw invalid("Payload thiếu trường hoặc sai kiểu", e);
        }
        if (payload.nonce().isBlank() || payload.kid().isBlank()) {
            throw invalid("nonce hoặc kid rỗng", null);
        }
        return new QrTokenParts(payloadJson, signature, payload);
    }

    private static byte[] decodeBase64Url(String part) {
        if (part.indexOf('=') >= 0) {
            throw invalid("base64url không được có padding", null);
        }
        try {
            return B64URL_DECODER.decode(part);
        } catch (IllegalArgumentException e) {
            throw invalid("base64url hỏng", e);
        }
    }

    private static QrTokenFormatException invalid(String message, Throwable cause) {
        return new QrTokenFormatException(Code.INVALID_FORMAT, message, cause);
    }
}
