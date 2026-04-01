package com.ootd.fitme.infrastructure.external.kakao.location;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "external.kakao")
@Validated
public record KakaoLocalProperties(
        @NotBlank String restApiKey,
        @Valid @NotNull Local local

) {
    public record Local(
            @NotBlank String baseUrl,
            @NotBlank String coord2regionPath) {

    }
}
