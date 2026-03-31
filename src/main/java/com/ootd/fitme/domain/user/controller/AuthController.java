package com.ootd.fitme.domain.user.controller;

import com.ootd.fitme.domain.user.dto.request.ResetPasswordRequest;
import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.service.UserService;
import com.ootd.fitme.global.security.jwt.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "refreshToken";

    private final UserService userService;
    private final JwtProperties jwtProperties;

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtDto> signInJson(@Valid @RequestBody SignInRequest signInRequest,
                                             HttpServletResponse response) {
        SignInResult result = userService.signIn(signInRequest);
        addRefreshCookie(response, result.refreshToken());
        return ResponseEntity.ok(result.jwtDto());
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JwtDto> signInMultipart (
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response
    ) {
        SignInResult result = userService.signIn(new SignInRequest(username, password));
        addRefreshCookie(response, result.refreshToken());
        return ResponseEntity.ok(result.jwtDto());
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken
    ) {
        JwtDto body = userService.refresh(refreshToken);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        userService.signOut(extractBearer(authorization), refreshToken);
        expireRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> csrfToken(CsrfToken csrfToken) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        userService.resetPassword(resetPasswordRequest);
        return ResponseEntity.noContent().build();
    }

    private String extractBearer(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length());
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(false) // 운영환경에서 true + HTTPS로 변경
                .path("/")
                .sameSite("Lax") // Strict/None 확인 필요
                .maxAge(Duration.ofMillis(jwtProperties.refreshTokenExpirationMs()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void expireRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(false) // 운영환경에서 true + HTTPS로 변경
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
