package com.ootd.fitme.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.cors")
public class SecurityCorsProperties {
    private List<String> allowedOrigins = List.of(
            "http://localhost:8080");
}
