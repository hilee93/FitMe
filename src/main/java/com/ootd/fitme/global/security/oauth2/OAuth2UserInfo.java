package com.ootd.fitme.global.security.oauth2;

public interface OAuth2UserInfo {
    String provider();
    String providerId();
    String email();
    String name();
}
