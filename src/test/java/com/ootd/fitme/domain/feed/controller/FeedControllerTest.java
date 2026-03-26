package com.ootd.fitme.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.exception.FeedNotFoundException;
import com.ootd.fitme.domain.feed.service.FeedService;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/feeds (피드생성)")
    class CreateFeedTest {

        @Test
        @DisplayName("[201] 유효한 요청이면 피드 생성 후 201 Created와 응답을 반환한다")
        void createFeed_success_when_valid_request() throws Exception {

            FeedCreateRequest request = new FeedCreateRequest(
                    UUID.randomUUID(),
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
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("테스트 피드"));
            then(feedService).should(times(1)).createFeed(request);
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
        @DisplayName("[200] 정상 feedId로 삭제 요청시 200 ok 반환한다")
        void deleteFeed_success_when_valid_request() throws Exception {

            //given
            UUID feedId = UUID.randomUUID();

            willDoNothing().given(feedService).deleteFeed(feedId);

            //when & then
            mockMvc.perform(delete("/api/feeds/{feedId}", feedId))
                    .andExpect(status().isNoContent());

            then(feedService).should(times(1)).deleteFeed(feedId);
        }

        @Test
        @DisplayName("[404] 유효하지않은 feedId로 삭제 요청시 404 Not Found 반환")
        void deleteFeed_fail() throws Exception {

            // given
            UUID feedId = UUID.randomUUID();

            willThrow(new FeedNotFoundException(ErrorCode.FEED_NOT_FOUND)).given(feedService).deleteFeed(feedId);

            // when & then
            mockMvc.perform(delete("/api/feeds/{feedId}", feedId))
                    .andExpect(status().isNotFound());

            then(feedService).should(times(1)).deleteFeed(feedId);
        }

    }

}