package com.ootd.fitme.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthSecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("실패 - CSRF 없이 로그인 요청하면 403")
    void signIn_withoutCsrf_return403() throws Exception {
        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "username": "tester@fitme.com",
                        "password": "password123!"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("성공 - csrf-token 선요청 후 로그인하면 200")
    void signIn_withCsrfToken_return200() throws Exception {
        UserDto userDto = new UserDto(
                UUID.randomUUID(),
                Instant.now(),
                "tester@fitme.com",
                "tester",
                Role.USER,
                false
        );

        JwtDto jwtDto = new JwtDto(userDto, "access-token");
        given(userService.signIn(any(SignInRequest.class)))
                .willReturn(new SignInResult(jwtDto, "refresh-token"));

        MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = csrfResult.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("XSRF-TOKEN=");

        String cookieToken = setCookie.split(";", 2)[0].split("=", 2)[1];
        String body = csrfResult.getResponse().getContentAsString();
        String headerToken = new ObjectMapper().readTree(body).get("token").asText();

        mockMvc.perform(post("/api/auth/sign-in")
                .cookie(new Cookie("XSRF-TOKEN", cookieToken))
                .header("X-XSRF-TOKEN", headerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "username": "tester@fitme.com",
                        "password": "password123!"
                        }
                        """))
                .andExpect(status().isOk());
    }
}
