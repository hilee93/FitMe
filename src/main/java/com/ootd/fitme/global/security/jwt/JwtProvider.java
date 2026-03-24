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
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.accessTokenExpirationMs());

        return Jwts.builder()
                .subject(userId.toString())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(ROLE_CLAIM, role)
                .signWith(secretKey)
                .compact();
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

    public String getRole(String token) {
        Object role = parseClaims(token).get(ROLE_CLAIM);
        return role == null ? null : role.toString();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
