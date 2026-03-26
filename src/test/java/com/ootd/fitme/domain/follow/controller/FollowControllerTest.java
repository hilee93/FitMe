package com.ootd.fitme.domain.follow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.follow.dto.request.FollowCreateRequest;
import com.ootd.fitme.domain.follow.dto.response.FollowDto;
import com.ootd.fitme.domain.follow.dto.response.FollowListResponse;
import com.ootd.fitme.domain.follow.dto.response.FollowSummaryDto;
import com.ootd.fitme.domain.follow.dto.response.UserSummary;
import com.ootd.fitme.domain.follow.enums.SortBy;
import com.ootd.fitme.domain.follow.enums.SortDirection;
import com.ootd.fitme.domain.follow.service.FollowService;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FollowController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
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

    @Nested
    @DisplayName("팔로잉 목록 조회")
    class GetFollowingsTest {

        @Test
        @DisplayName("성공 - 팔로잉 목록 조회 시 200을 반환한다")
        void getFollowing_request_return200() throws Exception {

            //given
            FollowListResponse response = new FollowListResponse(
                    List.of(), null, null, false, 0L,
                    SortBy.createdAt, SortDirection.DESCENDING);

            given(followService.getFollowings(any(), any(), any(), anyInt(), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/follows/followings")
                            .param("followerId", followerId.toString())
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
        @DisplayName("실패 - followerId가 없으면 400을 반환한다")
        void getFollowing_notExistFollowerId_return400() throws Exception {

            //when & then
            mockMvc.perform(get("/api/follows/followings")
                            .param("limit", "10"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - limit가 없으면 400을 반환한다")
        void getFollowing_notExistLimit_return400() throws Exception {

            //when & then
            mockMvc.perform(get("/api/follows/followings")
                            .param("followerId", followerId.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("팔로워 목록 조회")
    class GetFollowersTest {

        @Test
        @DisplayName("성공 - 팔로워 목록 조회 시 200을 반환한다")
        void getFollowers_request_return200() throws Exception {

            //given
            FollowListResponse response = new FollowListResponse(
                    List.of(), null, null, false, 0L,
                    SortBy.createdAt, SortDirection.DESCENDING);

            given(followService.getFollowers(any(), any(), any(), anyInt(), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/follows/followers")
                            .param("followeeId", followeeId.toString())
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
        @DisplayName("실패 - followeeId가 없으면 400을 반환한다")
        void getFollowers_notExistFolloweeId_return400() throws Exception {

            //when & then
            mockMvc.perform(get("/api/follows/followers")
                            .param("limit", "10"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - limit가 없으면 400을 반환한다")
        void getFollowers_notExistLimit_return400() throws Exception {

            //when & then
            mockMvc.perform(get("/api/follows/followers")
                            .param("followerId", followerId.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("팔로우 요약 조회")
    class GetFollowSummaryTest {

        @Test
        @DisplayName("성공 - 팔로우 요약 조회 시 200을 반환한다")
        void getFollowSummary_request_return200() throws Exception {

            //given
            UUID userId = UUID.randomUUID();
            UUID followerByMeId = UUID.randomUUID();

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(userId);
            given(mockUser.getEmail()).willReturn("test@test.com");
            given(mockUser.isLocked()).willReturn(false);
            given(mockUser.getRole()).willReturn(Role.USER);

            CustomUserPrincipal principal = CustomUserPrincipal.from(mockUser);

            FollowSummaryDto response = new FollowSummaryDto(
                    userId, 1, 0,
                    false, followerByMeId, false);

            given(followService.getFollowSummary(any(), any())).willReturn(response);

            //when & then
            mockMvc.perform(get("/api/follows/summary")
                    .param("userId", userId.toString())
                    .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.followeeId").value(userId.toString()))
                    .andExpect(jsonPath("$.followerCount").value(1))
                    .andExpect(jsonPath("$.followingCount").value(0))
                    .andExpect(jsonPath("$.followedByMe").value(false))
                    .andExpect(jsonPath("$.followedByMeId").value(followerByMeId.toString()))
                    .andExpect(jsonPath("$.followingMe").value(false));
        }
    }

}