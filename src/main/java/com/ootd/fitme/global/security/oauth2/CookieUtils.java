package com.ootd.fitme.global.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public final class CookieUtils {
    private CookieUtils(){
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie ->  cookie.getName().equals(name))
                .findFirst();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // prod HTTPS에서 true
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
