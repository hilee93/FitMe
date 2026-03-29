package com.ootd.fitme.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.user.dto.request.ChangePasswordRequest;
import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.request.UserLockUpdateRequest;
import com.ootd.fitme.domain.user.dto.request.UserRoleUpdateRequest;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.user.service.UserService;
import com.ootd.fitme.global.security.jwt.JwtProvider;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("User API 보안/인증 통합 테스트")
class UserSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    private UUID adminId;
    private UUID userId;
    private UUID anotherUserId;

    private String adminToken;
    private String userToken;
    private String anotherUserToken;

    @BeforeEach
    void setUp() {
        User admin = User.create("admin-security@fitme.com", "encoded");
        admin.updateRole(Role.ADMIN);
        userRepository.save(admin);

        User user = User.create("user-security@fitme.com", "encoded");
        user.updateRole(Role.USER);
        userRepository.save(user);

        User anotherUser = User.create("another-security@fitme.com", "encoded");
        anotherUser.updateRole(Role.USER);
        userRepository.save(anotherUser);

        adminId = admin.getId();
        userId = user.getId();
        anotherUserId = anotherUser.getId();

        adminToken = jwtProvider.generateAccessToken(adminId, "ROLE_ADMIN");
        userToken = jwtProvider.generateAccessToken(userId, "ROLE_USER");
        anotherUserToken = jwtProvider.generateAccessToken(anotherUserId, "ROLE_USER");
    }

    @Nested
    @DisplayName("POST /api/users")
    class SignUpSecurity {
        @Test
        @DisplayName("성공 - 인증 없이 회원가입 가능")
        void signUp_withoutAuth_returns201() throws Exception {
            UserCreateRequest request = new UserCreateRequest("tester", "tester@fitme.com", "password123!");
            UserDto response = new UserDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    "tester@fitme.com",
                    "tester",
                    Role.USER,
                    false
            );
            given(userService.signUp(any(UserCreateRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/role")
    class UpdateRoleSecurity {
        @Test
        @DisplayName("실패 - 미인증은 401")
        void updateRole_unauthenticated_returns401() throws Exception {
            mockMvc.perform(patch("/api/users/{userId}/role", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UserRoleUpdateRequest(Role.ADMIN))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - USER 권한은 403")
        void updateRole_userRole_returns403() throws Exception {
            mockMvc.perform(patch("/api/users/{userId}/role", userId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UserRoleUpdateRequest(Role.ADMIN))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("성공 - ADMIN 권한은 200")
        void updateRole_admin_returns200() throws Exception {
            given(userService.updateRole(eq(userId), any(UserRoleUpdateRequest.class)))
                    .willReturn(new UserDto(userId, Instant.now(), "user-security@fitme.com", "user", Role.ADMIN, false));

            mockMvc.perform(patch("/api/users/{userId}/role", userId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UserRoleUpdateRequest(Role.ADMIN))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/lock")
    class UpdateLockSecurity {
        @Test
        @DisplayName("실패 - 미인증은 401")
        void updateLock_unauthenticated_returns401() throws Exception {
            mockMvc.perform(patch("/api/users/{userId}/lock", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UserLockUpdateRequest(true))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - USER 권한은 403")
        void updateLock_userRole_returns403() throws Exception {
            mockMvc.perform(patch("/api/users/{userId}/lock", userId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UserLockUpdateRequest(true))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("성공 - ADMIN 권한은 200")
        void updateLock_admin_returns200() throws Exception {
            given(userService.updateLock(eq(userId), any(UserLockUpdateRequest.class)))
                    .willReturn(new UserDto(userId, Instant.now(), "user-security@fitme.com", "user", Role.USER, true));

            mockMvc.perform(patch("/api/users/{userId}/lock", userId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UserLockUpdateRequest(true))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{userId}/password")
    class ChangePasswordSecurity {
        @Test
        @DisplayName("실패 - 미인증은 401")
        void changePassword_unauthenticated_returns401() throws Exception {
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ChangePasswordRequest("new-password123!"))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 다른 USER의 비밀번호 변경은 403")
        void changePassword_otherUser_returns403() throws Exception {
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + anotherUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ChangePasswordRequest("new-password123!"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("성공 - 본인 USER는 204")
        void changePassword_selfUser_returns204() throws Exception {
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ChangePasswordRequest("new-password123!"))))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - ADMIN도 타 사용자 비밀번호 변경은 403")
        void changePassword_admin_otherUser_returns403() throws Exception {
            mockMvc.perform(patch("/api/users/{userId}/password", userId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ChangePasswordRequest("new-password123!"))))
                    .andExpect(status().isForbidden());
        }
    }
}
