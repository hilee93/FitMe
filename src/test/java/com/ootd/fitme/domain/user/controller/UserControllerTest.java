package com.ootd.fitme.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.user.dto.request.*;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.dto.response.UserDtoCursorResponse;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.enums.SortDirection;
import com.ootd.fitme.domain.user.service.UserService;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Nested
    @DisplayName("GET /api/users")
    class GetUsers {
        @Test
        @DisplayName("성공 - 사용자 목록 조회 시 200 반환")
        void getUsers_success() throws Exception {
            UUID userId = UUID.randomUUID();

            UserDtoCursorResponse response = new UserDtoCursorResponse(
                    List.of(savedUser(userId, Role.USER, false)),
                    "2026-04-09T00:00:00Z",
                    userId,
                    true,
                    10L,
                    "createdAt",
                    SortDirection.DESCENDING
            );

            given(userService.getUsers(any(UserSearchCondition.class))).willReturn(response);

            mockMvc.perform(get("/api/users")
                    .param("limit", "20")
                    .param("sortBy", "createdAt")
                    .param("sortDirection", "DESCENDING")
                    .param("emailLike", "tester"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value(userId.toString()))
                    .andExpect(jsonPath("$.hasNext").value(true))
                    .andExpect(jsonPath("$.totalCount").value(10))
                    .andExpect(jsonPath("$.sortBy").value("createdAt"))
                    .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

            ArgumentCaptor<UserSearchCondition> captor = ArgumentCaptor.forClass(UserSearchCondition.class);
            then(userService).should().getUsers(captor.capture());

            UserSearchCondition captured = captor.getValue();
            assertThat(captured.limit()).isEqualTo(20);
            assertThat(captured.emailLike()).isEqualTo("tester");
        }
    }
}
