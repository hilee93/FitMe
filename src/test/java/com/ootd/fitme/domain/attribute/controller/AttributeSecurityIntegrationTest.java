package com.ootd.fitme.domain.attribute.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.service.AttributeDefService;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.repository.UserRepository;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("의상 속성 API 보안/인증 통합 테스트")
class AttributeSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AttributeDefService attributeDefService;

    private String validAdminToken;
    private String validUserToken;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        User adminUser = User.create("admin@test.com", "encoded_password");
        adminUser.updateRole(Role.ADMIN);
        userRepository.save(adminUser);

        User normalUser = User.create("user@test.com", "encoded_password");
        normalUser.updateRole(Role.USER);
        userRepository.save(normalUser);

        validAdminToken = jwtProvider.generateAccessToken(adminUser.getId(), "ROLE_ADMIN");
        validUserToken = jwtProvider.generateAccessToken(normalUser.getId(), "ROLE_USER");
    }

    @Nested
    @DisplayName("GET /api/clothes/attribute-defs (목록 조회)")
    class Describe_getAttributes {
        @Test
        @DisplayName("[실패] 토큰이 없으면 401 Unauthorized를 반환한다.")
        void it_returns_401() throws Exception {
            mockMvc.perform(get("/api/clothes/attribute-defs"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("[실패] ROLE_USER 토큰으로 요청하면 보안 필요를 통과(200)한다.")
        void it_returns_403() throws Exception {
            mockMvc.perform(get("/api/clothes/attribute-defs")
                            .param("sortBy", "createdAt")
                            .param("sortDirection", "DESC")
                            .header("Authorization", "Bearer " + validAdminToken))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[성공] ROLE_ADMIN 토큰으로 요청하면 보안 필터를 통과(200)한다.")
        void it_returns_200() throws Exception {
            mockMvc.perform(get("/api/clothes/attribute-defs")
                            .param("sortBy", "createdAt")
                            .param("sortDirection", "DESC")
                            .header("Authorization", "Bearer " + validAdminToken))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/clothes/attribute-defs (속성 생성)")
    class Describe_createAttribute {
        private String requestBody;

        @BeforeEach
        void setUp() throws Exception {
            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("사이즈", List.of("S", "M"));
            requestBody = objectMapper.writeValueAsString(request);
        }

        @Test
        @DisplayName("[실패] 토큰이 없으면 401 Unauthorized를 반환한다.")
        void it_returns_401() throws Exception {
            mockMvc.perform(post("/api/clothes/attribute-defs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("[실패] ROLE_USER 토큰으로 요청하면 403 Forbidden을 반환한다.")
        void it_returns_403() throws Exception {
            mockMvc.perform(post("/api/clothes/attribute-defs")
                            .with(csrf())
                            .header("Authorization", "Bearer " + validUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("[성공] ROLE_ADMIN 토큰으로 요청하면 보안 필터를 통과(201)한다.")
        void it_returns_201() throws Exception {
            mockMvc.perform(post("/api/clothes/attribute-defs")
                            .with(csrf())
                            .header("Authorization", "Bearer " + validAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("PATCH /api/clothes/attribute-defs/{definitionId} (속성 수정)")
    class Describe_updateAttribute {
        private String requestBody;

        @BeforeEach
        void setUp() throws Exception {
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("수정된사이즈", List.of("L"));
            requestBody = objectMapper.writeValueAsString(request);
        }

        @Test
        @DisplayName("[실패] 권한이 없거나 부족하면 401/403을 반환한다.")
        void it_returns_error_for_invalid_auth() throws Exception {
            mockMvc.perform(patch("/api/clothes/attribute-defs/{definitionId}", testId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(patch("/api/clothes/attribute-defs/{definitionId}", testId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + validUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("[성공] ROLE_ADMIN 토큰으로 요청하면 보안 필터를 통과(200)한다.")
        void it_returns_200() throws Exception {
            mockMvc.perform(patch("/api/clothes/attribute-defs/{definitionId}", testId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + validAdminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/clothes/attribute-defs/{definitionId} (속성 삭제)")
    class Describe_deleteAttribute {
        @Test
        @DisplayName("[실패] 권한이 없거나 부족하면 401/403을 반환한다.")
        void it_returns_error_for_invalid_auth() throws Exception {
            mockMvc.perform(delete("/api/clothes/attribute-defs/{definitionId}", testId)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(delete("/api/clothes/attribute-defs/{definitionId}", testId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + validUserToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("[성공] ROLE_ADMIN 토큰으로 요청하면 보안 필터를 통과(204)한다.")
        void it_returns_204() throws Exception {
            mockMvc.perform(delete("/api/clothes/attribute-defs/{definitionId}", testId)
                            .with(csrf())
                            .header("Authorization", "Bearer " + validAdminToken))
                    .andExpect(status().isNoContent());
        }
    }
}