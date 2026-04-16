package com.ootd.fitme.global.security.oauth2;

import com.ootd.fitme.global.config.SecurityCorsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OAuth2AuthenticationFailureHandlerUnitTest {
    @Test
    @DisplayName("실패 - error 쿼리로 리다이렉트하고 oauth2_auth_request 쿠키 삭제")
    void onAuthenticationFailure_redirectError() throws Exception {
        SecurityCorsProperties corsProperties = new SecurityCorsProperties();
        corsProperties.setAllowedOrigins(List.of("http://localhost:8080"));

        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler(corsProperties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:8080");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, mock(AuthenticationException.class));

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:8080/auth/login?error=oauth2_login_failed");

        assertThat(response.getHeaders("Set-Cookie"))
                .anyMatch(v -> v.contains("oauth2_auth_request=") && v.contains("Max-Age=0"));
    }
}
