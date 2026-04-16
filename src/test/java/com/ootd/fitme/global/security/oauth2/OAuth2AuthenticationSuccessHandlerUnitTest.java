package com.ootd.fitme.global.security.oauth2;

import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.service.AuthService;
import com.ootd.fitme.global.config.SecurityCorsProperties;
import com.ootd.fitme.global.security.jwt.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OAuth2AuthenticationSuccessHandlerUnitTest {
    @Test
    @DisplayName("성공 - refresh 쿠키 설정 후 추천 페이지로 리다이렉트")
    void onAuthenticationSuccess_success() throws Exception {
        SecurityCorsProperties corsProperties = new SecurityCorsProperties();
        corsProperties.setAllowedOrigins(List.of("http://localhost:8080"));

        AuthService authService = mock(AuthService.class);
        JwtProperties jwtProperties = new JwtProperties(
                "mySecretKeyForJwtTokenGeneration1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                900000L,
                1209600000L,
                "fitme"
        );

        OAuth2AuthenticationSuccessHandler handler =
                new OAuth2AuthenticationSuccessHandler(corsProperties, authService, jwtProperties);

        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto(
                userId,
                Instant.now(),
                "social@test.com",
                "social",
                Role.USER,
                false
        );

        JwtDto jwtDto = new JwtDto(userDto, "access-token-value");
        SignInResult signInResult = new SignInResult(jwtDto, "refresh-token-value");

        given(authService.signInByUserId(userId)).willReturn(signInResult);

        OAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of("userId", userId.toString(), "id", "kakao-id"),
                "id"
        );

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:8080");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:8080/recommendations");

        List<String> setCookies = response.getHeaders("Set-Cookie");
        assertThat(setCookies).anyMatch(v -> v.contains("refreshToken=refresh-token-value"));
        assertThat(setCookies).anyMatch(v -> v.contains("oauth2_auth_request=") && v.contains("Max-Age=0"));

        verify(authService).signInByUserId(userId);
    }

    @Test
    @DisplayName("실패 - 처리 예외 발생 시 error 쿼리로 리다이렉트")
    void onAuthenticationSuccess_fail_redirectError() throws Exception {
        SecurityCorsProperties corsProperties = new SecurityCorsProperties();
        corsProperties.setAllowedOrigins(List.of("http://localhost:8080"));

        AuthService authService = mock(AuthService.class);
        JwtProperties jwtProperties = new JwtProperties(
                "mySecretKeyForJwtTokenGeneration1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                900000L,
                1209600000L,
                "fitme"
        );

        OAuth2AuthenticationSuccessHandler handler =
                new OAuth2AuthenticationSuccessHandler(corsProperties, authService, jwtProperties);

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn("not-oauth2-principal");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:8080");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:8080/auth/login?error=oauth2_login_failed");
    }
}
