package com.ootd.fitme.global.config;

import com.ootd.fitme.infrastructure.external.kakao.location.KakaoLocalProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KakaoLocalProperties.class)
public class KakaoPropertiesConfig {
}
