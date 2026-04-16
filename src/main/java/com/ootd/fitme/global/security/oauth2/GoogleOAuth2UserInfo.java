package com.ootd.fitme.global.security.oauth2;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String provider() {
        return "google";
    }

    @Override
    public String providerId() {
        return getString("sub");
    }

    @Override
    public String email() {
        return getString("email");
    }

    @Override
    public String name() {
        String name = getString("name");
        return (name == null || name.isBlank()) ? "google_user" : name;
    }

    private String getString(String key) {
        Object value = attributes.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
