package com.ootd.fitme.global.security.oauth2;

import com.ootd.fitme.global.config.AppRuntimePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class OAuth2AuthenticationFailureHandlerUnitTest {
    @Test
    @DisplayName("실패 - error 쿼리로 리다이렉트하고 oauth2_auth_request 쿠키 삭제")
    void onAuthenticationFailure_redirectError() throws Exception {
        AppRuntimePolicy runtimePolicy = mock(AppRuntimePolicy.class);

        given(runtimePolicy.resolveClientBaseUrl(any())).willReturn("http://localhost:8080");
        given(runtimePolicy.isSecureRequest(any())).willReturn(false);

        OAuth2AuthenticationFailureHandler handler = new OAuth2AuthenticationFailureHandler(runtimePolicy);

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
