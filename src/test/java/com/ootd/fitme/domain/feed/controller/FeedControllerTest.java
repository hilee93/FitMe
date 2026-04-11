package com.ootd.fitme.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.comment.dto.request.CommentSearchCondition;
import com.ootd.fitme.domain.comment.dto.response.CommentCursorResponseDto;
import com.ootd.fitme.domain.comment.dto.response.CommentFlatRow;
import com.ootd.fitme.domain.comment.dto.response.CommentResponseDto;
import com.ootd.fitme.domain.comment.enums.CommentSortCriteria;
import com.ootd.fitme.domain.comment.enums.SortDirection;
import com.ootd.fitme.domain.comment.service.CommentService;
import com.ootd.fitme.domain.feed.dto.request.FeedCommentCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.request.FeedUpdateRequestDto;
import com.ootd.fitme.domain.feed.dto.response.*;
import com.ootd.fitme.domain.feed.enums.FeedSortCriteria;
import com.ootd.fitme.domain.feed.exception.FeedAccessDeniedException;
import com.ootd.fitme.domain.feed.exception.FeedLikeAlreadyExistsException;
import com.ootd.fitme.domain.feed.exception.FeedLikeNotFoundException;
import com.ootd.fitme.domain.feed.exception.FeedNotFoundException;
import com.ootd.fitme.domain.feed.service.FeedService;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.auth.CustomUserPrincipal;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FeedController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;


    // NOTE: MockMvc 테스트에서 인증된 사용자 요청을 만들기 위해 SecurityContext에 Authentication(principal)을 설정한다.
    private RequestPostProcessor userPrincipal(UUID userId) {
        return request -> {
            CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
            given(principal.getUserId()).willReturn(userId);
            given(principal.getAuthorities()).willReturn(List.of());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }

    @Nested
    @DisplayName("POST /api/feeds (피드생성)")
    class CreateFeedTest {

        @Test
        @DisplayName("[201] 유효한 요청이면 피드 생성 후 201 Created와 응답을 반환한다")
        void createFeed_success_when_valid_request() throws Exception {

            UUID userId = UUID.randomUUID();

            FeedCreateRequest request = new FeedCreateRequest(
                    userId,
                    UUID.randomUUID(),
                    List.of(UUID.randomUUID()),
                    "테스트 피드"
            );

            FeedResponseDto response = new FeedResponseDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    Instant.now(),
                    null,
                    null,
                    List.of(),
                    "테스트 피드",
                    0,
                    0,
                    false
            );
            given(feedService.createFeed(any())).willReturn(response);

            mockMvc.perform(post("/api/feeds")
                            .with(userPrincipal(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("테스트 피드"));
            then(feedService).should(times(1)).createFeed(any());
        }

        @Test
        @DisplayName("[400] 피드생성 요청시 clothesIds가 비어있으면 MethodArgumentNotValidException과 400 Bad Request를 반환한다")
        void createFeed_fail_when_empty_clothesIds() throws Exception {
            String json = """
                    {
                      "authorId": "123e4567-e89b-12d3-a456-426614174000",
                      "weatherId": "123e4567-e89b-12d3-a456-426614174001",
                      "clothesIds": [],
                      "content": "테스트"
                    }
                    """;

            mockMvc.perform(post("/api/feeds")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> {
                        assertThat(result.getResolvedException())
                                .isInstanceOf(MethodArgumentNotValidException.class);
                    })
                    .andExpect(jsonPath("$.exceptionType").value(MethodArgumentNotValidException.class.getSimpleName()))
                    .andExpect(jsonPath("$.details.clothesIds").value("must not be empty"))
            ;
        }
    }

    @Nested
    @DisplayName("DELETE /api/feeds (피드삭제)")
    class DeleteFeedTest {

        @Test
        @DisplayName("[204] 정상 feedId로 삭제 요청시 204 noContent 반환한다")
        void deleteFeed_success_when_valid_request() throws Exception {

            //given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willDoNothing().given(feedService).deleteFeed(feedId, userId);

            //when & then
            mockMvc.perform(delete("/api/feeds/{feedId}", feedId)
                            .with(userPrincipal(userId))
                    )
                    .andExpect(status().isNoContent());

            then(feedService).should(times(1)).deleteFeed(feedId, userId);
        }

        @Test
        @DisplayName("[404] 유효하지않은 feedId로 삭제 요청시 404 Not Found 반환")
        void deleteFeed_fail() throws Exception {

            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willThrow(new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND)).given(feedService).deleteFeed(feedId, userId);

            // when & then
            mockMvc.perform(delete("/api/feeds/{feedId}", feedId)
                            .with(userPrincipal(userId))
                    )
                    .andExpect(status().isNotFound());

            then(feedService).should(times(1)).deleteFeed(feedId, userId);
        }

    }

    @Nested
    @DisplayName("PATCH /api/feeds (피드수정)")
    class UpdateFeedTest {
        @Test
        @DisplayName("[200] 작성자가 피드 수정 요청 시 200 OK와 content 수정된 FeedResponseDto 반환한다")
        void updateFeed_success_when_author() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FeedUpdateRequestDto request = new FeedUpdateRequestDto("수정된 내용");

            FeedResponseDto response = new FeedResponseDto(
                    feedId,
                    Instant.now(),
                    Instant.now(),
                    null, null, null,
                    "수정된 내용",
                    0,
                    0,
                    false
            );

            given(feedService.updateFeed(eq(feedId), eq(userId), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/feeds/{feedId}", feedId)
                            .with(userPrincipal(userId)) // 커스텀 principal 세팅
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("수정된 내용"));
        }

        @Test
        @DisplayName("[403] 작성자가 아닌 사용자가 수정 요청 시 403 Forbidden을 반환한다")
        void updateFeed_fail_when_not_author() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FeedUpdateRequestDto request = new FeedUpdateRequestDto("수정된 내용");

            given(feedService.updateFeed(eq(feedId), eq(userId), any()))
                    .willThrow(new FeedAccessDeniedException(ErrorCode.FEED_ACCESS_DENIED));

            // when & then
            mockMvc.perform(patch("/api/feeds/{feedId}", feedId)
                            .with(userPrincipal(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("[404] 존재하지 않는 피드 수정 요청 시 404 Not Found를 반환한다")
        void updateFeed_fail_when_feed_not_found() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FeedUpdateRequestDto request = new FeedUpdateRequestDto("수정된 내용");

            given(feedService.updateFeed(eq(feedId), eq(userId), any()))
                    .willThrow(new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND));

            // when & then
            mockMvc.perform(patch("/api/feeds/{feedId}", feedId)
                            .with(userPrincipal(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/feeds/{feedId}/like (피드 좋아요 생성)")
    class LikeFeedTest {

        @Test
        @DisplayName("[204] 좋아요 요청 시 204 OK를 반환한다")
        void likeFeed_success() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willDoNothing().given(feedService).likeFeed(feedId, userId);

            // when & then
            mockMvc.perform(post("/api/feeds/{feedId}/like", feedId)
                            .with(userPrincipal(userId)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[404] 존재하지 않는 피드 좋아요 요청 시 FeedNotFoundException이 발생하고 404를 반환한다")
        void likeFeed_fail_when_feed_not_found() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willThrow(new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND))
                    .given(feedService).likeFeed(feedId, userId);

            // when & then
            mockMvc.perform(post("/api/feeds/{feedId}/like", feedId)
                            .with(userPrincipal(userId)))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResolvedException())
                                    .isInstanceOf(FeedNotFoundException.class));
        }

        @Test
        @DisplayName("[409] 이미 좋아요한 피드에 다시 요청 시 FeedLikeAlreadyExistsException이 발생하고 409를 반환한다")
        void likeFeed_fail_when_feed_like_already_exists() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willThrow(new FeedLikeAlreadyExistsException(ErrorCode.FEED_LIKE_ALREADY_EXISTS))
                    .given(feedService).likeFeed(feedId, userId);

            // when & then
            mockMvc.perform(post("/api/feeds/{feedId}/like", feedId)
                            .with(userPrincipal(userId)))
                    .andExpect(status().isConflict())
                    .andExpect(result ->
                            assertThat(result.getResolvedException())
                                    .isInstanceOf(FeedLikeAlreadyExistsException.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/feeds/{feedId}/like (피드 좋아요 취소)")
    class UnlikeFeedTest {

        @Test
        @DisplayName("[204] 좋아요 취소 요청 시 204 NoContent를 반환한다")
        void unlikeFeed_success() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willDoNothing().given(feedService).unlikeFeed(feedId, userId);

            // when & then
            mockMvc.perform(delete("/api/feeds/{feedId}/like", feedId)
                            .with(userPrincipal(userId)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[404] 존재하지 않는 피드 좋아요 취소 요청 시 FeedNotFoundException이 발생하고 404를 반환한다")
        void unlikeFeed_fail_when_feed_not_found() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();


            willThrow(new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND))
                    .given(feedService).unlikeFeed(feedId, userId);

            // when & then
            mockMvc.perform(delete("/api/feeds/{feedId}/like", feedId)
                            .with(userPrincipal(userId)))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResolvedException())
                                    .isInstanceOf(FeedNotFoundException.class));
        }

        @Test
        @DisplayName("[404] 좋아요가 없는 상태에서 취소 요청 시 FeedLikeNotFoundException이 발생하고 404를 반환한다")
        void unlikeFeed_fail_when_feed_like_not_found() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            willThrow(new FeedLikeNotFoundException(ErrorCode.FEED_LIKE_NOT_FOUND))
                    .given(feedService).unlikeFeed(feedId, userId);

            // when & then
            mockMvc.perform(delete("/api/feeds/{feedId}/like", feedId)
                            .with(userPrincipal(userId)))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResolvedException())
                                    .isInstanceOf(FeedLikeNotFoundException.class));
        }
    }

    @Nested
    @DisplayName("POST /api/feeds/{feedId}/comments (댓글 등록)")
    class CreateCommentTest {

        @Test
        @DisplayName("[200] 정상 요청 시 댓글 생성 후 200 Created 반환")
        void createComment_success() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID commentId = UUID.randomUUID();

            FeedCommentCreateRequest request = new FeedCommentCreateRequest(
                    feedId,
                    userId,
                    "댓글 내용"
            );

            CommentResponseDto response = CommentResponseDto.from(
                    new CommentFlatRow(
                            commentId,
                            Instant.now(),
                            feedId,
                            userId,
                            "name",
                            null,
                            "댓글 내용"
                    )
            );

            given(commentService.createFeedComment(any(FeedCommentCreateRequest.class), eq(userId)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
                            .with(userPrincipal(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(commentId.toString()))
                    .andExpect(jsonPath("$.content").value("댓글 내용"));

            then(commentService).should(times(1))
                    .createFeedComment(any(FeedCommentCreateRequest.class), eq(userId));
        }

        @Test
        @DisplayName("[400] content가 비어있으면 400 Bad Request")
        void createComment_fail_when_empty_content() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FeedCommentCreateRequest request = new FeedCommentCreateRequest(
                    feedId,
                    userId,
                    ""
            );

            // when & then
            mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
                            .with(userPrincipal(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResolvedException())
                                    .isInstanceOf(MethodArgumentNotValidException.class));
        }

        @Test
        @DisplayName("[404] 존재하지 않는 피드에 댓글 작성 시 404 Not Found 반환")
        void createComment_fail_when_feed_not_found() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FeedCommentCreateRequest request = new FeedCommentCreateRequest(
                    feedId,
                    userId,
                    "댓글 내용"
            );

            given(commentService.createFeedComment(any(FeedCommentCreateRequest.class), eq(userId)))
                    .willThrow(new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/feeds/{feedId}/comments", feedId)
                            .with(userPrincipal(userId))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            then(commentService).should(times(1))
                    .createFeedComment(any(FeedCommentCreateRequest.class), eq(userId));
        }
    }

    @Nested
    @DisplayName("GET /api/feeds/${feedId}/comments (피드 댓글목록 조회)")
    class getFeedCommentsTest {

        @Test
        @DisplayName("[200] feedId와 limit가 유효하면 피드 댓글 목록 조회에 성공한다")
        void getFeedComments_success_when_valid_request() throws Exception {

            // given
            UUID feedId = UUID.randomUUID();

            CommentCursorResponseDto response = new CommentCursorResponseDto(
                    List.of(),
                    null,
                    null,
                    false,
                    0L,
                    CommentSortCriteria.CREATED_AT.getValue(),
                    SortDirection.DESCENDING
            );

            given(commentService.getFeedComments(any(CommentSearchCondition.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/feeds/{feedId}/comments", feedId)
                            .with(userPrincipal(UUID.randomUUID()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("limit", "20")
                            .param("feedId", feedId.toString())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalCount").value(0))
                    .andExpect(jsonPath("$.sortBy").value("createdAt"))
                    .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

        }

        @Test
        @DisplayName("[200] cursor와 idAfter가 있으면 다음 페이지 댓글 목록을 조회한다")
        void getFeedComments_next_page_success_when_valid_request() throws Exception {
            // given
            UUID feedId = UUID.randomUUID();
            UUID idAfter = UUID.randomUUID();
            String cursor = "2026-03-31T00:00:00Z";

            CommentCursorResponseDto response = new CommentCursorResponseDto(
                    List.of(),
                    null,
                    null,
                    false,
                    30L,
                    CommentSortCriteria.CREATED_AT.getValue(),
                    SortDirection.DESCENDING
            );

            given(commentService.getFeedComments(any(CommentSearchCondition.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/feeds/{feedId}/comments", feedId)
                            .with(userPrincipal(UUID.randomUUID()))
                            .param("feedId", feedId.toString())
                            .param("limit", "20")
                            .param("cursor", cursor)
                            .param("idAfter", idAfter.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalCount").value(30))
                    .andExpect(jsonPath("$.sortBy").value("createdAt"))
                    .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

            verify(commentService).getFeedComments(argThat(condition ->
                    condition.feedId().equals(feedId) &&
                            condition.limit().equals(20) &&
                            cursor.equals(condition.cursor()) &&
                            idAfter.equals(condition.idAfter())
            ));
        }

        @Test
        @DisplayName("[400] limit가 없으면 검증에 실패한다")
        void getFeedComments_fail_when_limit_is_null() throws Exception {
            UUID feedId = UUID.randomUUID();

            mockMvc.perform(get("/api/feeds/{feedId}/comments", feedId)
                            .with(userPrincipal(UUID.randomUUID()))
                            .param("feedId", feedId.toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[400] idAfter가 UUID 형식이 아니면 실패한다")
        void getFeedComments_fail_when_idAfter_invalid() throws Exception {
            UUID feedId = UUID.randomUUID();

            mockMvc.perform(get("/api/feeds/{feedId}/comments", feedId)
                            .with(userPrincipal(UUID.randomUUID()))
                            .param("feedId", feedId.toString())
                            .param("limit", "20")
                            .param("idAfter", "invalid-uuid"))
                    .andExpect(status().isBadRequest());
        }


    }

    @Nested
    @DisplayName("GET /api/feeds/{feedId}/comments (피드 조회)")
    class searchFeedsTest {
        @Test
        @DisplayName("[200] 기본 조건으로 피드 목록을 조회한다")
        void searchFeeds_success_when_default_request() throws Exception {
            // given
            UUID userId = UUID.randomUUID();

            FeedResponseDto feed1 = createFeedResponseDto(
                    UUID.randomUUID(),
                    Instant.parse("2026-04-02T10:00:00Z"),
                    "첫 번째 피드"
            );

            FeedResponseDto feed2 = createFeedResponseDto(
                    UUID.randomUUID(),
                    Instant.parse("2026-04-02T09:59:00Z"),
                    "두 번째 피드"
            );

            FeedCursorResponseDto response = new FeedCursorResponseDto(
                    List.of(feed1, feed2),
                    null,
                    null,
                    false,
                    2,
                    FeedSortCriteria.CREATED_AT.getValue(),
                    com.ootd.fitme.domain.feed.enums.SortDirection.DESCENDING
            );

            given(feedService.searchFeeds(any(FeedSearchCondition.class), eq(userId)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/feeds")
                            .with(userPrincipal(userId))
                            .param("sortBy", "createdAt")
                            .param("sortDirection", "DESCENDING")
                            .param("limit", "20")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(feed1.id().toString()))
                    .andExpect(jsonPath("$.data[0].content").value("첫 번째 피드"))
                    .andExpect(jsonPath("$.data[0].likeCount").value(3))
                    .andExpect(jsonPath("$.data[0].commentCount").value(1))
                    .andExpect(jsonPath("$.data[0].likedByMe").value(true))
                    .andExpect(jsonPath("$.data[1].id").value(feed2.id().toString()))
                    .andExpect(jsonPath("$.hasNext").value(false))
                    .andExpect(jsonPath("$.totalCount").value(2))
                    .andExpect(jsonPath("$.sortBy").value("createdAt"))
                    .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

            ArgumentCaptor<FeedSearchCondition> captor =
                    ArgumentCaptor.forClass(FeedSearchCondition.class);

            verify(feedService).searchFeeds(captor.capture(), eq(userId));

            FeedSearchCondition condition = captor.getValue();
            assertThat(condition.sortBy()).isEqualTo(FeedSortCriteria.CREATED_AT);
            assertThat(condition.sortDirection()).isEqualTo(com.ootd.fitme.domain.feed.enums.SortDirection.DESCENDING);
        }

        @Test
        @DisplayName("[400] 필수 파라미터가 없으면 피드 목록 조회에 실패한다")
        void searchFeeds_fail_when_required_params_missing() throws Exception {
            mockMvc.perform(get("/api/feeds")
                            .with(userPrincipal(UUID.randomUUID())))
                    .andExpect(status().isBadRequest());
        }
    }

    private FeedResponseDto createFeedResponseDto(UUID feedId, Instant createdAt, String content) {
        return new FeedResponseDto(
                feedId,
                createdAt,
                createdAt,
                createAuthorSummaryDto(),
                createWeatherSummaryDto(),
                List.of(),
                content,
                3,
                1,
                true
        );
    }

    private FeedAuthorSummaryDto createAuthorSummaryDto() {
        return new FeedAuthorSummaryDto(
                UUID.randomUUID(),
                "tester",
                "https://example.com/profile.jpg"
        );
    }

    private FeedWeatherSummaryDto createWeatherSummaryDto() {
        return new FeedWeatherSummaryDto(
                UUID.randomUUID(),
                SkyStatus.CLEAR,
                new FeedPrecipitationSummaryDto(
                        PrecipitationType.NONE,
                        0.0,
                        0.0
                ),
                new FeedTemperatureSummaryDto(
                        0.0,
                        0.0,
                        0.0,
                        0.0
                )
        );
    }


}