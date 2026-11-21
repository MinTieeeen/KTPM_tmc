package com.tmc.gate.access.domain;

/** Sức khỏe gần nhất của cổng xoay; không gọi mạng (tách khỏi {@link TurnstileGateway} – ISP). */
public interface TurnstileHealthReader {

    TurnstileHealth health(String turnstileId);
}
