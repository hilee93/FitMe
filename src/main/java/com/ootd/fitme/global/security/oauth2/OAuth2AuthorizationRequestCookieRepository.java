package com.ootd.fitme.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Base64;

@Component
public class OAuth2AuthorizationRequestCookieRepository
implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
                .map(cookie -> deserialize(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            CookieUtils.deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
            return;
        }

        String serialized = serialize(authorizationRequest);
        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        CookieUtils.deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        return authorizationRequest;
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(authorizationRequest);
            oos.flush();
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("oauth2 authorizationRequest serialize 실패", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            byte[] bytes = Base64.getDecoder().decode(value);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                Object deserialized = ois.readObject();
                if (deserialized instanceof OAuth2AuthorizationRequest request) {
                    return request;
                }
                return null;
            }
        } catch (IllegalArgumentException | IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
