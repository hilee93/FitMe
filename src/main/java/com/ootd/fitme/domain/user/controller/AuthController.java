package com.ootd.fitme.domain.user.controller;

import com.ootd.fitme.domain.user.dto.request.ResetPasswordRequest;
import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.service.AuthService;
import com.ootd.fitme.domain.user.service.UserService;
import com.ootd.fitme.global.config.AppRuntimePolicy;
import com.ootd.fitme.global.security.jwt.JwtProperties;
import com.ootd.fitme.global.security.oauth2.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;
    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final AppRuntimePolicy runtimePolicy;

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtDto> signInJson(@Valid @RequestBody SignInRequest signInRequest,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        SignInResult result = authService.signIn(signInRequest);
        addRefreshCookie(response, result.refreshToken(), request);
        return ResponseEntity.ok(result.jwtDto());
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JwtDto> signInMultipart (
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        SignInResult result = authService.signIn(new SignInRequest(username, password));
        addRefreshCookie(response, result.refreshToken(), request);
        return ResponseEntity.ok(result.jwtDto());
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken
    ) {
        JwtDto body = authService.refresh(refreshToken);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        authService.signOut(extractBearer(authorization), refreshToken);
        expireRefreshCookie(response, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, String>> csrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok(Map.of(
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName(),
                "token", csrfToken.getToken()
        ));
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

    private void addRefreshCookie(HttpServletResponse response, String token, HttpServletRequest request) {
        int maxAgeSeconds = Math.toIntExact(
                Duration.ofMillis(jwtProperties.refreshTokenExpirationMs()).getSeconds()
        );
        CookieUtils.addCookie(
                response,
                REFRESH_COOKIE,
                token,
                maxAgeSeconds,
                runtimePolicy.isSecureRequest(request)
        );
    }

    private void expireRefreshCookie(HttpServletResponse response, HttpServletRequest request) {
        CookieUtils.deleteCookie(
                response,
                REFRESH_COOKIE,
                runtimePolicy.isSecureRequest(request)
        );
    }
}
