package com.ootd.fitme.global.config;

import com.ootd.fitme.infrastructure.external.openweather.OpenWeatherProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenWeatherProperties.class)
public class OpenWeatherPropertiesConfig {
}
