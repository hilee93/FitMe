package com.ootd.fitme.domain.attribute.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefCreateRequest;
import com.ootd.fitme.domain.attribute.dto.request.ClothesAttributeDefUpdateRequest;
import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;
import com.ootd.fitme.domain.attribute.exception.AttributeException;
import com.ootd.fitme.domain.attribute.service.AttributeDefService;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.auth.CustomUserDetailsService;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import com.ootd.fitme.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AttributeDefController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
))
@DisplayName("AttributeController 단위 테스트")
@AutoConfigureMockMvc(addFilters = false)
class AttributeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttributeDefService service;

    private UUID attributeId;
    private ClothesAttributeDefDto mockDto;

    @BeforeEach
    void setUp() {
        attributeId = UUID.randomUUID();
        mockDto = new ClothesAttributeDefDto(attributeId, "사이즈", List.of("S", "M", "L"), Instant.now());
    }

    @Nested
    @DisplayName("GET /api/clothes/attribute-defs (목록 조회)")
    class Describe_getAttributes {
        @Test
        @WithMockUser
        @DisplayName("[성공] 속성 목록을 200 OK와 함께 반환한다.")
        void it_returns_200() throws Exception {
            // given
            given(service.getClothesAttributeDefs(any(), any(), any())).willReturn(List.of(mockDto));

            // when & then
            mockMvc.perform(get("/api/clothes/attribute-defs")
                            .param("sortBy", "createdAt")
                            .param("sortDirection", "DESC"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(attributeId.toString()))
                    .andExpect(jsonPath("$[0].name").value("사이즈"))
                    .andExpect(jsonPath("$[0].selectableValues.length()").value(3));
        }
    }

    @Nested
    @DisplayName("POST /api/clothes/attribute-defs (속성 생성)")
    class Describe_createAttribute {
        @Test
        @WithMockUser
        @DisplayName("[성공] 올바른 요청 시 속성을 생성하고 201 Created를 반환한다.")
        void it_returns_201() throws Exception {
            // given
            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("사이즈", List.of("S", "M"));
            given(service.createClothesAttributeDef(any(ClothesAttributeDefCreateRequest.class))).willReturn(mockDto);

            // when & then
            mockMvc.perform(post("/api/clothes/attribute-defs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("사이즈"));
        }

        @Test
        @WithMockUser
        @DisplayName("[실패] 이미 존재하는 이름이면 서비스에서 발생한 예외가 던져진다.")
        void it_throws_duplicate_exception() throws Exception {
            ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("중복사이즈", List.of("S"));
            given(service.createClothesAttributeDef(any(ClothesAttributeDefCreateRequest.class)))
                    .willThrow(new AttributeException(ErrorCode.ATTRIBUTE_NAME_DUPLICATED));

            mockMvc.perform(post("/api/clothes/attribute-defs")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("ATTR_003"));
        }
    }

    @Nested
    @DisplayName("PUT /api/clothes/attribute-defs/{definitionId} (속성 수정)")
    class Describe_updateAttribute {
        @Test
        @WithMockUser
        @DisplayName("[성공] 정보 수정 후 200 OK를 반환한다.")
        void it_returns_200() throws Exception {
            ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("수정된사이즈", List.of("L"));
            given(service.updateClothesAttributeDef(eq(attributeId), any(ClothesAttributeDefUpdateRequest.class))).willReturn(mockDto);

            mockMvc.perform(patch("/api/clothes/attribute-defs/{definitionId}", attributeId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/clothes/attribute-defs{definitionId} (속성 삭제)")
    class Describe_deleteAttribute {
        @Test
        @WithMockUser
        @DisplayName("[성공] 속성 삭제 후 204 No Content를 반환한다.")
        void it_returns_204() throws Exception {
            mockMvc.perform(delete("/api/clothes/attribute-defs/{definitionId}", attributeId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        @DisplayName("[실패] 존재하지 않는 ID면 예외가 던져진다.")
        void it_throws_not_found() throws Exception {
            doThrow(new AttributeException(ErrorCode.ATTRIBUTE_NOT_FOUND))
                    .when(service).deleteClothesAttributeDef(attributeId);

            mockMvc.perform(delete("/api/clothes/attribute-defs/{definitionId}", attributeId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ATTR_001"));
        }
    }
}