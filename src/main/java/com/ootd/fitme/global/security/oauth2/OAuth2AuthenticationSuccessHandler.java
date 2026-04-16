package com.ootd.fitme.global.security.oauth2;

import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.service.AuthService;
import com.ootd.fitme.global.config.AppRuntimePolicy;
import com.ootd.fitme.global.security.jwt.JwtProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final String REFRESH_COOKIE = "refreshToken";
    private static final String SUCCESS_PATH = "/recommendations";
    private static final String FAILURE_PATH = "/auth/login";

    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final AppRuntimePolicy runtimePolicy;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CookieUtils.deleteCookie(
                response,
                OAuth2AuthorizationRequestCookieRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME,
                runtimePolicy.isSecureRequest(request)
        );

        try {
            UUID userId = extractUserId(authentication);
            SignInResult result = authService.signInByUserId(userId);

            int maxAgeSeconds = Math.toIntExact(
                    Duration.ofMillis(jwtProperties.refreshTokenExpirationMs()).toSeconds()
            );

            CookieUtils.addCookie(
                    response,
                    REFRESH_COOKIE,
                    result.refreshToken(),
                    maxAgeSeconds,
                    runtimePolicy.isSecureRequest(request)
            );

            String targetUrl = UriComponentsBuilder.fromUriString(runtimePolicy.resolveClientBaseUrl(request))
                    .path(SUCCESS_PATH)
                    .build(true)
                    .toUriString();

            response.sendRedirect(targetUrl);
        } catch (RuntimeException e) {
            log.warn("[OAUTH2][SUCCESS_HANDLER_FAIL] uri={}, msg={}",
                    request.getRequestURI(),
                    e.getMessage(),
                    e);
            String targetUrl = UriComponentsBuilder.fromUriString(runtimePolicy.resolveClientBaseUrl(request))
                    .path(FAILURE_PATH)
                    .queryParam("error", "oauth2_login_failed")
                    .build(true)
                    .toUriString();
            response.sendRedirect(targetUrl);
        }
    }

    private UUID extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof OAuth2User oauth2User)) {
            throw new IllegalStateException("oauth2 principal type mismatch");
        }

        Map<String, Object> attributes = oauth2User.getAttributes();
        Object userId = attributes.get("userId");
        if (userId == null) {
            throw new IllegalStateException("missing userid attribute");
        }

        return UUID.fromString(String.valueOf(userId));
    }
}
