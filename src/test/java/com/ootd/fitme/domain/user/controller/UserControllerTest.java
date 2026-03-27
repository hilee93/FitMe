package com.ootd.fitme.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.user.dto.request.ChangePasswordRequest;
import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.request.UserLockUpdateRequest;
import com.ootd.fitme.domain.user.dto.request.UserRoleUpdateRequest;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.service.UserService;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    private UserDto savedUser(UUID userId, Role role, boolean locked) {
        return new UserDto(
                userId,
                Instant.parse("2026-03-27T00:00:00Z"),
                "tester@fitme.com",
                "tester",
                role,
                locked
        );
    }

    @Nested
    @DisplayName("POST /api/users")
    class SignUpTest {
        @Test
        @DisplayName("성공 - 회원가입 시 201 반환")
        void signUp_success() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDto userDto = savedUser(userId, Role.USER, false);

            given(userService.signUp(any(UserCreateRequest.class))).willReturn(userDto);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                             {"name":"tester","email":"tester@fitme.com","password":"password123!"}
                             """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(userId.toString()))
                    .andExpect(jsonPath("$.email").value("tester@fitme.com"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.locked").value(false));

            then(userService).should().signUp(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("실패 - 이메일 형식 오류면 400 반환")
        void signUp_invalidEmail_return400() throws Exception {
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                             {"name":"tester","email":"invalid-email","password":"password123!"}
                             """))
                    .andExpect(status().isBadRequest());

            then(userService).should(never()).signUp(any(UserCreateRequest.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/role")
    class UpdateRoleTest {
        @Test
        @DisplayName("성공 - 권한 변경 시 200 반환")
        void updateRole_success() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDto userDto = savedUser(userId, Role.ADMIN, false);

            given(userService.updateRole(eq(userId),any(UserRoleUpdateRequest.class))).willReturn(userDto);

            mockMvc.perform(patch("/api/users/{userId}/role", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"role":"ADMIN"}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId.toString()))
                    .andExpect(jsonPath("$.role").value("ADMIN"));

            then(userService).should().updateRole(eq(userId),any(UserRoleUpdateRequest.class));
        }

        @Test
        @DisplayName("실패 - role이 null이면 400 반환")
        void updateRole_nullRole_return400() throws Exception {
            UUID userId = UUID.randomUUID();

            mockMvc.perform(patch("/api/users/{userId}/role", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"role":null}
                            """))
                    .andExpect(status().isBadRequest());

            then(userService).should(never()).updateRole(any(UUID.class),any(UserRoleUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/lock")
    class UpdateLockTest {
        @Test
        @DisplayName("성공 - 잠금 상태 변경 시 200 반환")
        void updateLock_success() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDto userDto = savedUser(userId, Role.USER, true);

            given(userService.updateLock(eq(userId), any(UserLockUpdateRequest.class))).willReturn(userDto);

            mockMvc.perform(patch("/api/users/{userId}/lock", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"locked":true}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId.toString()))
                    .andExpect(jsonPath("$.locked").value(true));

            then(userService).should().updateLock(eq(userId), any(UserLockUpdateRequest.class));
        }

        @Test
        @DisplayName("실패 - locked가 null이면 400 반환")
        void updateLock_nullLock_return400() throws Exception {
            UUID userId = UUID.randomUUID();

            mockMvc.perform(patch("/api/users/{userId}/lock", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"locked":null}
                            """))
                    .andExpect(status().isBadRequest());

            then(userService).should(never()).updateLock(any(UUID.class), any(UserLockUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/password")
    class UpdatePasswordTest {
        @Test
        @DisplayName("성공 - 비밀번호 변경 시 204 반환")
        void updatePassword_success() throws Exception {
            UUID userId = UUID.randomUUID();

            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"password":"new-password123!"}
                            """))
                    .andExpect(status().isNoContent());

            then(userService).should().changePassword(eq(userId), any(ChangePasswordRequest.class));
        }

        @Test
        @DisplayName("실패 - password가 blank면 400 반환")
        void updatePassword_blank_return400() throws Exception {
            UUID userId = UUID.randomUUID();

            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"password":""}
                            """))
                    .andExpect(status().isBadRequest());

            then(userService).should(never()).changePassword(any(UUID.class), any(ChangePasswordRequest.class));
        }
    }
}
