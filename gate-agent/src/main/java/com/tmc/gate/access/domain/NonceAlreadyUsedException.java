package com.tmc.gate.access.domain;

/**
 * Nonce đã được ghi nhận khi ghi lượt PENDING – xảy ra khi hai yêu cầu đồng thời dùng cùng token.
 * Store ném ngoại lệ này và không ghi gì.
 */
public class NonceAlreadyUsedException extends RuntimeException {

    public NonceAlreadyUsedException(String message) {
        super(message);
    }
}
