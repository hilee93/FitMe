package com.ootd.fitme.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * jwt.* 키를 안전하게 받고 하드코딩을 방지
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secretKey,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs,
        String issuer
) {
}
