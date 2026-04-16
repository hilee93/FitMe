package com.ootd.fitme.global.security.oauth2;

import com.ootd.fitme.global.config.AppRuntimePolicy;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final String FAILURE_PATH = "/auth/login";

    private final AppRuntimePolicy runtimePolicy;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        CookieUtils.deleteCookie(
                response,
                OAuth2AuthorizationRequestCookieRepository.OAUTH2_AUTH_REQUEST_COOKIE_NAME,
                runtimePolicy.isSecureRequest(request)
        );

        String targetUrl = UriComponentsBuilder.fromUriString(runtimePolicy.resolveClientBaseUrl(request))
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
}
