package com.ootd.fitme.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.user.dto.request.ResetPasswordRequest;
import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.service.AuthService;
import com.ootd.fitme.domain.user.service.UserService;
import com.ootd.fitme.global.config.AppRuntimePolicy;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import com.ootd.fitme.global.security.jwt.JwtProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private AppRuntimePolicy runtimePolicy;

    @Nested
    @DisplayName("POST /api/auth/sign-in")
    class SignInTest {

        @Test
        @DisplayName("성공 - 로그인 시 200과 refresh 쿠키를 반환한다")
        void signIn_success() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDto userDto = new UserDto(
                    userId,
                    Instant.parse("2026-03-27T00:00:00Z"),
                    "tester@fitme.com",
                    "tester",
                    Role.USER,
                    false
            );
            JwtDto jwtDto = new JwtDto(userDto, "access-token-value");
            SignInResult result = new SignInResult(jwtDto, "refresh-token-value");

            given(authService.signIn(any(SignInRequest.class))).willReturn(result);
            given(jwtProperties.refreshTokenExpirationMs()).willReturn(604800000L);

            mockMvc.perform(post("/api/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "tester@fitme.com",
                                      "password": "password123!"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token-value"))
                    .andExpect(jsonPath("$.userDto.id").value(userId.toString()))
                    .andExpect(header().string("Set-Cookie", containsString("refreshToken=refresh-token-value")))
                    .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                    .andExpect(header().string("Set-Cookie", containsString("Max-Age=604800")));

            then(authService).should().signIn(any(SignInRequest.class));
        }

        @Test
        @DisplayName("실패 - username이 없으면 400을 반환한다")
        void signIn_fail_whenUsernameBlank() throws Exception {
            mockMvc.perform(post("/api/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "username": "",
                                      "password": "password123!"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());

            then(authService).should(never()).signIn(any(SignInRequest.class));
        }

        @Test
        @DisplayName("성공 - multipart/form-data 로그인 시 200과 refresh 쿠키를 반환")
        void signIn_multipart_success() throws Exception {
            UserDto userDto = new UserDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    "tester@fitme.com",
                    "tester",
                    Role.USER,
                    false
            );

            JwtDto jwtDto = new JwtDto(userDto, "access-token");
            SignInResult result = new SignInResult(jwtDto, "refresh-token");

            given(authService.signIn(any(SignInRequest.class))).willReturn(result);

            mockMvc.perform(multipart("/api/auth/sign-in")
                    .param("username", "tester@fitme.com")
                    .param("password", "password123!")
                    .with(req -> {
                        req.setMethod("POST");
                        return req;
                    }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")));

            then(authService).should().signIn(any(SignInRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshTest {

        @Test
        @DisplayName("성공 - refreshToken 쿠키로 access 토큰 재발급 시 200을 반환한다")
        void refresh_success() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDto userDto = new UserDto(
                    userId,
                    Instant.parse("2026-03-27T00:00:00Z"),
                    "tester@fitme.com",
                    "tester",
                    Role.USER,
                    false
            );
            JwtDto jwtDto = new JwtDto(userDto, "new-access-token");
            given(authService.refresh("refresh-token-value")).willReturn(jwtDto);

            mockMvc.perform(post("/api/auth/refresh")
                            .cookie(new Cookie("refreshToken", "refresh-token-value")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"));

            then(authService).should().refresh("refresh-token-value");
        }
    }

    @Nested
    @DisplayName("POST /api/auth/sign-out")
    class SignOutTest {

        @Test
        @DisplayName("성공 - 로그아웃 시 204와 refresh 쿠키 만료 헤더를 반환한다")
        void signOut_success() throws Exception {
            mockMvc.perform(post("/api/auth/sign-out")
                            .header("Authorization", "Bearer access-token-value")
                            .cookie(new Cookie("refreshToken", "refresh-token-value")))
                    .andExpect(status().isNoContent())
                    .andExpect(header().string("Set-Cookie", containsString("refreshToken=")))
                    .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

            then(authService).should().signOut(eq("access-token-value"), eq("refresh-token-value"));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/csrf-token")
    class CsrfTokenTest {

        @Test
        @DisplayName("성공 - csrf 토큰 조회 요청 시 200를 반환한다")
        void csrfToken_success() throws Exception {
            mockMvc.perform(get("/api/auth/csrf-token")
                            .requestAttr(CsrfToken.class.getName(),
                                    new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-csrf-token")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                    .andExpect(jsonPath("$.parameterName").value("_csrf"))
                    .andExpect(jsonPath("$.token").value("test-csrf-token"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/reset-password")
    class ResetPasswordTest {

        @Test
        @DisplayName("성공 - 이메일로 임시 비밀번호 발급 요청 시 204를 반환한다")
        void resetPassword_success() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest("tester@fitme.com");

            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            then(userService).should().resetPassword(eq(request));
        }

        @Test
        @DisplayName("실패 - 이메일 형식이 잘못되면 400을 반환한다")
        void resetPassword_fail_whenInvalidEmail() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest("invalid-email");

            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            then(userService).should(never()).resetPassword(any(ResetPasswordRequest.class));
        }
    }
}
