package com.ootd.fitme.domain.follow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.UserSummary;
import com.ootd.fitme.domain.follow.service.FollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FollowService followService;

    private UUID followerId;
    private UUID followeeId;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followeeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("팔로우 생성")
    class FollowCreateTest {

        @Test
        @DisplayName("성공 - 팔로우 요청 시 201을 반환한다")
        void createFollow_request_return201() throws Exception {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
            UserSummary followerSummary = new UserSummary(followerId, "follower", null);
            UserSummary followeeSummary = new UserSummary(followeeId, "followee", null);
            FollowDto response = new FollowDto(UUID.randomUUID(), followeeSummary, followerSummary);

            given(followService.createFollow(any())).willReturn(response);

            //when & then
            mockMvc.perform(post("/api/follows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.followee.userId").value(followeeId.toString()))
                    .andExpect(jsonPath("$.follower.userId").value(followerId.toString()));
        }
        @Test
        @DisplayName("실패 - followerId가 null이면 400을 반환한다")
        void createFollow_nullFollowerId_return400() throws Exception {

            //given
            FollowCreateRequest request = new FollowCreateRequest(null, followeeId);

            //when & then
            mockMvc.perform(post("/api/follows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - followeeId가 null이면 400을 반환한다")
        void createFollow_nullFolloweeId_return400() throws Exception {

            //given
            FollowCreateRequest request = new FollowCreateRequest(followerId, null);

            //when & then
            mockMvc.perform(post("/api/follows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("팔로우 취소")
    class FollowCancelTest {

        @Test
        @DisplayName("성공 - 취소 요청 시 204를 반환한다")
        void cancelFollow_request_return204() throws Exception {

            //given
            UUID followId = UUID.randomUUID();

            //when & then
            mockMvc.perform(delete("/api/follows/{followId}", followId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 존재하지 않은 팔로우 취소 시 400을 반환한다")
        void cancelFollow_notExistFollowId_return400() throws Exception {

            //given
            UUID followId = UUID.randomUUID();

            willThrow(new IllegalArgumentException("존재하지 않는 팔로우입니다."))
                    .given(followService).cancelFollow(any());


            //when & then
            mockMvc.perform(delete("/api/follows/{followId}", followId))
                    .andExpect(status().isBadRequest());
        }

    }



}