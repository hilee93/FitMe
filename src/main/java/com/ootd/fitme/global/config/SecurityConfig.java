package com.ootd.fitme.global.config;

import com.ootd.fitme.global.security.exception.CustomAccessDeniedHandler;
import com.ootd.fitme.global.security.exception.CustomAuthenticationEntryPoint;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        PathPatternRequestMatcher.Builder path = PathPatternRequestMatcher.withDefaults();
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
                                path.matcher(HttpMethod.POST, "/api/users"),
                                path.matcher(HttpMethod.POST, "/api/auth/sign-in"),
                                path.matcher(HttpMethod.POST, "/api/auth/sign-out"),
                                path.matcher(HttpMethod.POST, "/api/auth/refresh"),
                                path.matcher(HttpMethod.POST, "/api/auth/reset-password")
                        )
                        .ignoringRequestMatchers(
                                "/h2-console/**"
                        )
                )
                .headers(headers ->
                        headers.frameOptions(frameOptions -> frameOptions.sameOrigin()) // 클릭재킹 거부(iframe) 같은 주소를 가진 프레임(iframe) 주소라면 허용, 이건 기본 서버사이드 일때 h2화면인 iframe으로 되어있어서 허용할려고하느거고 실제 분리된 프론트에선 피료없음
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 허용
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(
                                // 정적 리스스를 위해 추가
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/vite.svg",
                                "/logo_symbol.svg",
                                "/favicon.ico",
                                "/error",
                                "/uploads/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/.well-known/**",
                                "/h2-console/**"
                        ).permitAll()
                        // CORS preflight 요청을 위한 설정
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                // API
                                "/api/users",
                                "/api/auth/sign-in",
                                "/api/auth/sign-out",
                                "/api/auth/refresh",
                                "/api/auth/reset-password",
                                "/api/auth/csrf-token",
                                "/api/sse",
                                "/ws/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clothes/attribute-defs/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/clothes/attribute-defs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .build();
    }
}
