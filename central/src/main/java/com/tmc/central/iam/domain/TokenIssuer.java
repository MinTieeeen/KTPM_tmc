package com.tmc.central.iam.domain;

import java.time.Instant;

/** Phát token truy cập cho tài khoản (cài đặt: JwtTokenIssuer, RS256). */
public interface TokenIssuer {

    IssuedToken issue(UserAccount account, Instant now);
}
