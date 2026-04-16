package com.ootd.fitme.global.security.oauth2;

import java.util.Locale;
import java.util.Map;

public final class OAuth2UserInfoFactory {
    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo create(String registrationId, Map<String, Object> attributes) {
        String provider = registrationId == null ? "" : registrationId.toLowerCase(Locale.ROOT);

        return switch (provider) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            default ->  throw new IllegalArgumentException("Unsupported oauth2 provider: " + registrationId);
        };
    }
}
