package com.ootd.fitme.global.security.oauth2;

import java.util.LinkedHashMap;
import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String provider() {
        return "kakao";
    }

    @Override
    public String providerId() {
        Object id = attributes.get("id");
        return id == null ? null : String.valueOf(id);
    }

    @Override
    public String email() {
        Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");

        if (kakaoAccount == null) {
            return null;
        }

        Object email = kakaoAccount.get("email");
        return email == null ? null : String.valueOf(email);
    }

    @Override
    public String name() {
        Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");

        if (kakaoAccount != null) {
            Map<String, Object> profile = getMap(kakaoAccount, "profile");
            if (profile != null) {
                Object nickname = profile.get("nickname");
                if (nickname != null && !String.valueOf(nickname).isBlank()) {
                    return String.valueOf(nickname);
                }
            }
        }
        String id = providerId();
        return (id == null || id.isBlank()) ? "kakao_user" : "kakao_" + id;
    }

    private Map<String, Object> getMap(Map<String, Object> source, String key) {
        Object value = source.get(key);

        if (!(value instanceof Map<?,?> rawMap)) {
            return null;
        }

        Map<String, Object> converted = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!(entry.getKey() instanceof String stringKey)) {
                return null;
            }
            converted.put(stringKey, entry.getValue());
        }
        return converted;
    }
}
