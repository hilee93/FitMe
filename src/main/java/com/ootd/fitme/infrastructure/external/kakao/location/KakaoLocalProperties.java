package com.ootd.fitme.infrastructure.external.kakao.location;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.kakao")
public record KakaoLocalProperties(
        String restApiKey,
        Local local

) {
    public record Local(String baseUrl, String coord2regionPath) {

    }
}
