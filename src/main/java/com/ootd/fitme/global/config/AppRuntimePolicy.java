package com.ootd.fitme.global.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AppRuntimePolicy {
    private static final List<String> LOCAL_ORIGINS = List.of(
            "http://localhost",
            "http://localhost:8080"
    );

    private static final List<String> PROD_ORIGINS = List.of(
            "https://fitme-nginx.mangofield-4b56edf3.koreacentral.azurecontainerapps.io"
    );

    private final Environment environment;

    public List<String> allowedOrigins() {
        return isProd() ? PROD_ORIGINS : LOCAL_ORIGINS;
    }

    public String resolveClientBaseUrl(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        List<String> allowedOrigins = allowedOrigins();

        if (origin != null && allowedOrigins.contains(origin)) {
            return origin;
        }

        return allowedOrigins.get(0);
    }

    public boolean isSecureRequest(HttpServletRequest request) {
        if (request.isSecure()) {
            return true;
        }

        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return "https".equalsIgnoreCase(forwardedProto);
    }

    private boolean isProd() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch("prod"::equalsIgnoreCase);
    }

}
