package com.ootd.fitme.global.security.oauth2;

import com.ootd.fitme.global.config.SecurityCorsProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final String FAILURE_PATH = "/auth/login";

    private final SecurityCorsProperties securityCorsProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        CookieUtils.deleteCookie(response, OAuth2AuthorizationRequestCookieRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME);

        String targetUrl = UriComponentsBuilder.fromUriString(resolveClientBaseUrl(request))
                .path(FAILURE_PATH)
                .queryParam("error", "oauth2_login_failed")
                .build(true)
                .toUriString();
        log.warn("[OAUTH2][FAIL] uri={}, exClass={}, msg={}",
                request.getRequestURI(),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception);
        response.sendRedirect(targetUrl);
    }

    private String resolveClientBaseUrl(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        List<String> allowedOrigins = securityCorsProperties.getAllowedOrigins();

        if (origin != null && allowedOrigins != null && allowedOrigins.contains(origin)) {
            return origin;
        }

        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            return allowedOrigins.get(0);
        }

        return "http://localhost:8080";
    }
}
