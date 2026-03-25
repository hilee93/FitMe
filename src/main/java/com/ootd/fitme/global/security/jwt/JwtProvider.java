package com.ootd.fitme.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/*
 * 토큰 생성 / 검증 등을 모아서 재사용성 확보
 */
@Component
public class JwtProvider {
    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String role) {
        return generateToken(userId, role, jwtProperties.accessTokenExpirationMs(), ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(UUID userId, String role) {
        return generateToken(userId, role, jwtProperties.refreshTokenExpirationMs(), REFRESH_TOKEN_TYPE);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String getTokenId(String token) {
        return parseClaims(token).getId();
    }

    public Instant getIssuedAt(String token) {
        return parseClaims(token).getIssuedAt().toInstant();
    }

    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public String getRole(String token) {
        Object role = parseClaims(token).get(ROLE_CLAIM);
        return role == null ? null : role.toString();
    }

    public boolean isRefreshToken(String token) {
        Object tokenType = parseClaims(token).get(TOKEN_TYPE_CLAIM);
        return tokenType != null && REFRESH_TOKEN_TYPE.equals(tokenType.toString());
    }

    public boolean isAccessToken(String token) {
        Object tokenType = parseClaims(token).get(TOKEN_TYPE_CLAIM);
        return tokenType != null && ACCESS_TOKEN_TYPE.equals(tokenType.toString());
    }

    private String generateToken(UUID userId, String role, long expirationMs, String tokenType) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti
                .subject(userId.toString())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(ROLE_CLAIM, role)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

