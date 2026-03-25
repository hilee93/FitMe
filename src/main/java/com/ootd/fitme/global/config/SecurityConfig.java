package com.ootd.fitme.global.config;

import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieName("XSRF-TOKEN");
        csrfTokenRepository.setHeaderName("X-XSRF-TOKEN");

        return http
                // CSRF 상세 정책은 인증/인가 단계 이후 별도 반영
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        // TODO: 보안 강화 단계에서 ignoringRequestMatchers 제거 예정
                        // csrf-token 엔드포인트는 제외하지 않음 ( 토큰 발급용)
                        .ignoringRequestMatchers(
                                "/api/auth/sign-in",
                                "/api/auth/sign-out",
                                "/api/auth/refresh",
                                "/api/auth/reset-password"
                        )
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/sign-in",
                                "/api/auth/sign-out",
                                "/api/auth/refresh",
                                "/api/auth/reset-password",
                                "/api/auth/csrf-token",
                                "/api/sse",
                                "/ws/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
