package com.ootd.fitme.global.config;

import com.ootd.fitme.global.interceptor.MDCLoggingInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${fitme.storage.local.root-path:./storage}")
    private String localDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MDCLoggingInterceptor())
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteStoragePath = Paths.get(localDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/storage/**")
                .addResourceLocations(absoluteStoragePath);
        log.info("[WebConfig] /storage/** {}", absoluteStoragePath);

        String absoluteUploadsPath = Paths.get("uploads").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absoluteUploadsPath);

        log.info("[WebConfig] /uploads/** {}", absoluteUploadsPath);


        // TODO: 운영 전환 시 local file 노출 제거하고 Azure 운영 방식으로 변경
    }
}
