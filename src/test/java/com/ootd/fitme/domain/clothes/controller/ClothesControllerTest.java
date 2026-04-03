package com.ootd.fitme.domain.clothes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.clothes.dto.ClothesDto;
import com.ootd.fitme.domain.clothes.dto.request.ClothesCreateRequest;
import com.ootd.fitme.domain.clothes.dto.request.ClothesDtoCursorRequest;
import com.ootd.fitme.domain.clothes.dto.response.ClothesDtoCursorResponse;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.clothes.enums.SortDirection;
import com.ootd.fitme.domain.clothes.exception.ClothesException;
import com.ootd.fitme.domain.clothes.service.ClothesService;
import com.ootd.fitme.global.exception.ErrorCode;

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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
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
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private UUID loginUserId;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        loginUserId = UUID.randomUUID();
        mockAuthentication = new TestingAuthenticationToken(loginUserId, null, "ROLE_USER");
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

            ClothesDto mockResponse = new ClothesDto(
                    UUID.randomUUID(), loginUserId, "흰 셔츠", null, ClothesType.TOP, List.of()
            );

            given(clothesService.createClothes(any(ClothesCreateRequest.class), any())).willReturn(mockResponse);

            // when & then
            mockMvc.perform(multipart("/api/clothes")
                            .file(requestPart)
                            .with(authentication(mockAuthentication))
                            .with(csrf())
                    )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("흰 셔츠"));

            then(clothesService).should(times(1)).createClothes(any(), any());
        }

        @Test
        @DisplayName("[실패] 본인이 아닌 타인의 ID로 옷 생성을 요청하면 403 Forbidden 예외를 던진다.")
        void createClothes_Fail_Forbidden() throws Exception {
            UUID maliciousTargetId = UUID.randomUUID();
            ClothesCreateRequest requestDto = new ClothesCreateRequest(
                    maliciousTargetId, "해킹된 셔츠", ClothesType.TOP, List.of()
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(requestDto)
            );

            // when & then
            mockMvc.perform(multipart("/api/clothes")
                            .file(requestPart)
                            .with(authentication(mockAuthentication))
                            .with(csrf())
                    )
                    .andDo(print())
                    .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResolvedException()).isInstanceOf(ClothesException.class))
                    .andExpect(result -> {
                        ClothesException ex = (ClothesException) result.getResolvedException();
                        org.assertj.core.api.Assertions.assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN);
                    });

            then(clothesService).shouldHaveNoInteractions();
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
                            .with(authentication(mockAuthentication))
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

            // when & then
            mockMvc.perform(multipart("/api/clothes/{clothesId}", clothesId)
                            .param("name", "수정된 자켓")
                            .with(request -> {
                                request.setMethod(HttpMethod.PATCH.name());
                                return request;
                            })
                            .with(authentication(mockAuthentication))
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
                            .with(authentication(mockAuthentication))
                            .with(csrf())
                    )
                    .andDo(print())
                    .andExpect(status().isNoContent());

            then(clothesService).should(times(1)).deleteClothes(eq(clothesId), eq(loginUserId));
        }
    }
}