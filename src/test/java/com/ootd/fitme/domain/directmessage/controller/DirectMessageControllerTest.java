package com.ootd.fitme.domain.directmessage.controller;

import com.ootd.fitme.domain.directmessage.dto.response.DirectMessageDtoCursorResponse;
import com.ootd.fitme.domain.directmessage.enums.SortBy;
import com.ootd.fitme.domain.directmessage.enums.SortDirection;
import com.ootd.fitme.domain.directmessage.service.DirectMessageService;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = DirectMessageController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class DirectMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DirectMessageService directMessageService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("DM 목록 조회")
    class GetDirectMessagesTest {

        @Test
        @DisplayName("성공 - DM 목록 조회 시 200을 반환한다")
        void getDirectMessages_request_return200() throws Exception {

            //given
            DirectMessageDtoCursorResponse response = new DirectMessageDtoCursorResponse(
                    List.of(), null, null,
                    false, 0, SortBy.createdAt, SortDirection.DESCENDING);

            given(directMessageService.getDirectMessages(any(), any(), any(), anyInt()))
                    .willReturn(response);

            //when & then
            mockMvc.perform(get("/api/direct-messages")
                    .param("userId", userId.toString())
                    .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.nextCursor").value(nullValue()))
                    .andExpect(jsonPath("$.nextIdAfter").value(nullValue()))
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalCount").value(0))
                    .andExpect(jsonPath("$.sortBy").value("createdAt"))
                    .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));
        }

        @Test
        @DisplayName("실패 - userId가 없으면 400을 반환한다")
        void getDirectMessages_notExistUserId_return400() throws Exception {

            //when & then
            mockMvc.perform(get("/api/direct-messages")
                            .param("limit", "10"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - limit가 없으면 400을 반환한다")
        void getDirectMessages_notExistLimit_return400() throws Exception {

            //when & then
            mockMvc.perform(get("/api/direct-messages")
                            .param("userId", userId.toString()))
                    .andExpect(status().isBadRequest());
        }
    }
}