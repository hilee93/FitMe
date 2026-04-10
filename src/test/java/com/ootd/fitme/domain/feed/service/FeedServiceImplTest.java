package com.ootd.fitme.domain.feed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.clothesattribute.repository.ClothesAttributeRepository;
import com.ootd.fitme.domain.clothesattributeselectablevalue.repository.ClothesAttributeSelectableValueRepository;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.request.FeedSearchCondition;
import com.ootd.fitme.domain.feed.dto.request.FeedUpdateRequestDto;
import com.ootd.fitme.domain.feed.dto.response.FeedBaseFlatRow;
import com.ootd.fitme.domain.feed.dto.response.FeedCursorResponseDto;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.enums.FeedSortCriteria;
import com.ootd.fitme.domain.feed.enums.SortDirection;
import com.ootd.fitme.domain.feed.exception.FeedAccessDeniedException;
import com.ootd.fitme.domain.feed.exception.FeedLikeAlreadyExistsException;
import com.ootd.fitme.domain.feed.exception.FeedLikeNotFoundException;
import com.ootd.fitme.domain.feed.exception.FeedNotFoundException;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder.FeedFixture;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder.FeedFixtureWithClothesDetails;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder.FeedFixtureWithoutClothes;
import com.ootd.fitme.domain.feed.repository.FeedRepository;
import com.ootd.fitme.domain.feedclothes.repository.FeedClothesRepository;
import com.ootd.fitme.domain.feedlike.entity.FeedLike;
import com.ootd.fitme.domain.feedlike.repository.FeedLikeRepository;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.selectablevalue.repository.SelectableValueRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class FeedServiceImplTest {

    @Autowired
    private FeedService feedService;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private FeedClothesRepository feedClothesRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private WeatherForecastRepository weatherForecastRepository;

    @Autowired
    private ClothesRepository clothesRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private SelectableValueRepository selectableValueRepository;

    @Autowired
    private ClothesAttributeRepository clothesAttributeRepository;

    @Autowired
    private ClothesAttributeSelectableValueRepository clothesAttributeSelectableValueRepository;

    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Autowired
    private EntityManager em;

    @Autowired
    private FeedLikeRepository feedLikeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(FeedServiceImplTest.class);




    @Nested
    @DisplayName("피드관리 - 피드생성")
    class CreateFeedTest {

        @Test
        @DisplayName("[Positive] 피드 생성 - Feed가 저장되고 응답값이 올바르게 반환된다")
        void createFeed_success_when_valid_request() {

            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            User user = feedFixture.user();
            WeatherForecast weather = feedFixture.weather();
            Clothes top = feedFixture.clothes();

            Clothes bottom = clothesRepository.save(
                    Clothes.create("하의", ClothesType.BOTTOM, user)
            );

            FeedCreateRequest request = new FeedCreateRequest(
                    user.getId(),
                    weather.getId(),
                    List.of(top.getId(), bottom.getId()),
                    "테스트 피드"
            );

            // when
            FeedResponseDto result = feedService.createFeed(request);

            // then (응답 검증)
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("테스트 피드");
            assertThat(result.author().userId()).isEqualTo(user.getId());
            assertThat(result.ootds()).hasSize(2);

            // then (DB 저장 검증)
            Feed savedFeed = feedRepository.findById(result.id()).orElseThrow();
            assertThat(savedFeed.getContent()).isEqualTo("테스트 피드");
            assertThat(savedFeed.getWeatherForecast().getId()).isEqualTo(weather.getId());
            assertThat(savedFeed.getUser().getId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("[Negative] 피드 생성 - 존재하지 않는 날씨 ID로 피드 생성 시 예외가 발생한다")
        void createFeed_fail_when_weather_not_found() {
            User user = userRepository.save(
                    User.create("email@test.com", "password")
            );

            FeedCreateRequest request = new FeedCreateRequest(
                    user.getId(),
                    UUID.randomUUID(),
                    List.of(),
                    "테스트 피드"
            );

            assertThatThrownBy(() -> feedService.createFeed(request))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("[Negative] 피드생성 - 존재하지 않는 작성자 ID로 피드 생성 시 예외가 발생한다")
        void createFeed_fail_when_user_not_found() {
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();

            FeedCreateRequest request = new FeedCreateRequest(
                    UUID.randomUUID(),
                    feedFixture.weather().getId(),
                    List.of(),
                    "테스트 피드"
            );

            assertThatThrownBy(() -> feedService.createFeed(request))
                    .isInstanceOf(UserException.class);
        }

    }

    @Nested
    @DisplayName("피드삭제")
    class DeleteFeedTest {

        @Test
        @DisplayName("[Positive] 피드삭제 - 정상 삭제후 해당 조회없음")
        void deleteFeed_success_when_valid_request() {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();

            // when
            feedService.deleteFeed(feedFixture.feed().getId());

            em.flush();
            em.clear(); // NOTE: feedClothes는 DB에서 CASCADE처리라 JPA 영속성은 모르기때문에 DB반영후 아래진행

            // then
            assertThat(feedRepository.findById(feedFixture.feed().getId())).isEmpty();
            assertThat(feedClothesRepository.findById(feedFixture.feedClothes().getId())).isEmpty();

        }

        @Test
        @DisplayName("[Negative] 피드삭제 - 없는 Id로 삭제시 FeedNotFoundException 예외발생")
        void deleteFeed_fail_when_feed_not_found() {
            assertThatThrownBy(() -> feedService.deleteFeed(UUID.randomUUID()))
                    .isExactlyInstanceOf(FeedNotFoundException.class)
                    .hasMessage(ErrorCode.FEED_NOT_FOUND.getMessage());
        }

    }

    @Nested
    @DisplayName("피드수정")
    class UpdateFeedTest {

        @Test
        @DisplayName("[Positive] 작성자가 피드 수정 요청 시 응답과 DB에 수정 내용이 반영된다")
        void updateFeed_success_when_valid_request() {

            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            UUID feedId = feed.getId();
            UUID userId = user.getId();

            FeedUpdateRequestDto request = new FeedUpdateRequestDto("수정된 내용");

            // when
            FeedResponseDto response = feedService.updateFeed(feedId, userId, request);

            // then 1: 응답 검증
            assertThat(response.content()).isEqualTo("수정된 내용");

            // then 2: DB 검증
            em.flush();
            em.clear();

            Feed reloadedFeed = feedRepository.findById(feedId).orElseThrow();
            assertThat(reloadedFeed.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("[Negative] 존재하지 않는 피드 수정 요청 시 FeedNotFoundException이 발생한다")
        void updateFeed_fail_when_feed_not_found() {
            // given
            UUID notExistsFeedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            FeedUpdateRequestDto request = new FeedUpdateRequestDto("수정된 내용");

            // when & then
            assertThatThrownBy(() -> feedService.updateFeed(notExistsFeedId, userId, request))
                    .isInstanceOf(FeedNotFoundException.class);
        }

        @Test
        @DisplayName("[Negative] 작성자가 아닌 사용자가 피드 수정 요청 시 FeedAccessDeniedException이 발생한다")
        void updateFeed_fail_when_user_is_not_author() {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();

            User otherUser = userRepository.save(
                    User.create("other@test.com", "password")
            );

            UUID feedId = feed.getId();
            UUID otherUserId = otherUser.getId();

            FeedUpdateRequestDto request = new FeedUpdateRequestDto("수정된 내용");

            // when & then
            assertThatThrownBy(() -> feedService.updateFeed(feedId, otherUserId, request))
                    .isInstanceOf(FeedAccessDeniedException.class);

            em.flush();
            em.clear();

            Feed reloadedFeed = feedRepository.findById(feedId).orElseThrow();
            assertThat(reloadedFeed.getContent()).isNotEqualTo("수정된 내용");
        }

    }

    @Nested
    @DisplayName("피드좋아요 생성")
    class LikeFeedTest {

        @Test
        @DisplayName("[Positive] 피드좋아요 - 피드 좋아요 요청시 FeedLike가 저장되고 likeCount가 증가한다")
        void likeFeed_success_when_valid_request() {

            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();

            User liker = userRepository.save(
                    User.create("liker-" + UUID.randomUUID() + "@test.com", "password")
            );

            UUID feedId = feed.getId();
            UUID likerId = liker.getId();

            // when
            feedService.likeFeed(feedId, likerId);

            // then
            em.flush();
            em.clear();

            assertThat(feedLikeRepository.existsByFeedIdAndUserId(feedId, likerId)).isTrue();

            Feed reloadedFeed = feedRepository.findById(feedId).orElseThrow();
            assertThat(reloadedFeed.getLikeCount()).isEqualTo(1);

        }

        @Test
        @DisplayName("[Negative] 존재하지 않는 피드 좋아요 요청 시 FeedNotFoundException이 발생한다")
        void likeFeed_fail_when_feed_not_found() {
            // given
            User liker = userRepository.save(
                    User.create("liker-" + UUID.randomUUID() + "@test.com", "password")
            );

            UUID notExistsFeedId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> feedService.likeFeed(notExistsFeedId, liker.getId()))
                    .isInstanceOf(FeedNotFoundException.class);
        }

        @Test
        @DisplayName("[Negative] 이미 좋아요한 피드에 다시 좋아요 요청 시 FeedLikeAlreadyExistsException이 발생한다")
        void likeFeed_fail_when_feed_like_already_exists() {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User author = feed.getUser();

            User liker = userRepository.save(
                    User.create("liker-" + UUID.randomUUID() + "@test.com", "password")
            );

            feedLikeRepository.save(
                    FeedLike.create(feed, liker)
            );

            // when & then
            assertThatThrownBy(() -> feedService.likeFeed(feed.getId(), liker.getId()))
                    .isInstanceOf(FeedLikeAlreadyExistsException.class);
        }

    }

    @Nested
    @DisplayName("피드좋아요 취소")
    class UnLikeFeedTest {

        @Test
        @DisplayName("[Positive] 피드좋아요 취소 - 좋아요 취소 요청 시 FeedLike가 삭제되고 likeCount가 감소한다")
        void unlikeFeed_success_when_valid_request() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();

            User liker = userRepository.save(
                    User.create("liker-" + UUID.randomUUID() + "@test.com", "password")
            );

            feedService.likeFeed(feed.getId(), liker.getId());

            em.flush();
            em.clear();

            // when
            feedService.unlikeFeed(feed.getId(), liker.getId());

            // then
            em.flush();
            em.clear();

            Feed reloadedFeed = feedRepository.findById(feed.getId()).orElseThrow();
            assertThat(reloadedFeed.getLikeCount()).isEqualTo(0);

            assertThat(feedLikeRepository.existsByFeedIdAndUserId(feed.getId(), liker.getId())).isFalse();
        }

        @Test
        @DisplayName("[Negative] 피드좋아요 취소 - 존재하지 않는 피드 취소 요청 시 FeedNotFoundException이 발생한다")
        void unlikeFeed_fail_when_feed_not_found() {
            // given
            User liker = userRepository.save(
                    User.create("liker-" + UUID.randomUUID() + "@test.com", "password")
            );

            UUID notExistsFeedId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> feedService.unlikeFeed(notExistsFeedId, liker.getId()))
                    .isInstanceOf(FeedNotFoundException.class);
        }

        @Test
        @DisplayName("[Negative] 피드좋아요 취소 - 좋아요가 없는 상태에서 취소 요청 시 예외가 발생한다")
        void unlikeFeed_fail_when_feed_like_not_found() {
            // given
            FeedFixtureBuilder.FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();

            User liker = userRepository.save(
                    User.create("liker-" + UUID.randomUUID() + "@test.com", "password")
            );

            // when & then
            assertThatThrownBy(() -> feedService.unlikeFeed(feed.getId(), liker.getId()))
                    .isInstanceOf(FeedLikeNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("피드 조회")
    class FeedSearchTest {

        @Test
        @DisplayName("[Positive] 피드 목록 조회 - 피드 목록 조회시 같은값 가짐")
        void searchFeeds_success() throws JsonProcessingException {
            // given

            FeedFixtureWithClothesDetails feedFixture = feedFixtureBuilder.createFeedFixtureWithClothesDetails();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            em.flush();
            em.clear();

            FeedSearchCondition condition = new FeedSearchCondition(
                    null,
                    FeedSortCriteria.CREATED_AT,
                    SortDirection.DESCENDING,
                    null,
                    null,
                    20,
                    null,
                    null,
                    null
                    );


            // when
            FeedCursorResponseDto result = feedService.searchFeeds(condition, user.getId());

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isEqualTo(1);


            assertThat(result.data())
                    .extracting(FeedResponseDto::id)
                    .containsExactly(feed.getId());

            FeedResponseDto target = result.data().stream()
                    .filter(feedResponse -> feedResponse.id().equals(feed.getId()))
                    .findFirst()
                    .orElseThrow();

            assertThat(target.content()).isEqualTo(feed.getContent());
            assertThat(target.likeCount()).isEqualTo(feed.getLikeCount());
            assertThat(target.commentCount()).isEqualTo(feed.getCommentCount());

            assertThat(target.author()).isNotNull();
            assertThat(target.author().userId()).isEqualTo(user.getId());

            assertThat(target.weather()).isNotNull();
            assertThat(target.ootds()).isNotEmpty();

            log.debug("result = \n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }


        @Test
        @DisplayName("[Positive] 피드 목록 조회 - 결과 없음")
        void searchFeeds_success_when_no_feed() throws JsonProcessingException {
            // given
            FeedSearchCondition condition = new FeedSearchCondition(
                    null,
                    FeedSortCriteria.CREATED_AT,
                    SortDirection.DESCENDING,
                    null,
                    null,
                    20,
                    null,
                    null,
                    null
            );

            // when
            FeedCursorResponseDto result = feedService.searchFeeds(condition, UUID.randomUUID());

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isEqualTo(0);

            log.debug("result = \n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }

        @Test
        @DisplayName("[Positive] 피드 목록 조회 - 옷 정보 없음")
        void searchFeeds_success_when_feed_has_no_ootds() throws JsonProcessingException {
// given
            FeedFixtureWithoutClothes feedFixture = feedFixtureBuilder.createFeedFixtureWithoutClothes();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();

            em.flush();
            em.clear();

            FeedSearchCondition condition = new FeedSearchCondition(
                    null,
                    FeedSortCriteria.CREATED_AT,
                    SortDirection.DESCENDING,
                    null,
                    null,
                    20,
                    null,
                    null,
                    null
            );

            // when
            FeedCursorResponseDto result = feedService.searchFeeds(condition, user.getId());

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.totalCount()).isEqualTo(1);

            FeedResponseDto target = result.data().stream()
                    .filter(feedResponse -> feedResponse.id().equals(feed.getId()))
                    .findFirst()
                    .orElseThrow();

            assertThat(target.content()).isEqualTo(feed.getContent());
            assertThat(target.author()).isNotNull();
            assertThat(target.weather()).isNotNull();
            assertThat(target.ootds()).isEmpty();

            log.debug("result = \n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }

        @Test
        @DisplayName("[Positive] 피드 목록 조회 - createdAt 내림차순 정렬")
        void searchFeeds_success_sorted_by_createdAt_desc() throws JsonProcessingException, InterruptedException {
            // given
            FeedFixtureWithClothesDetails olderFixture = feedFixtureBuilder.createFeedFixtureWithClothesDetails();
            Feed olderFeed = olderFixture.feed();
            User user = olderFixture.user();


            em.flush();

            Thread.sleep(10);

            FeedFixtureWithClothesDetails newerFixture = feedFixtureBuilder.createFeedFixtureWithClothesDetails();
            Feed newerFeed = newerFixture.feed();

            em.flush();
            em.clear();

            FeedSearchCondition condition = new FeedSearchCondition(
                    null,
                    FeedSortCriteria.CREATED_AT,
                    SortDirection.DESCENDING,
                    null,
                    null,
                    20,
                    null,
                    null,
                    null
            );

            // when
            FeedCursorResponseDto result = feedService.searchFeeds(condition, user.getId());

            // then
            assertThat(result.data()).hasSize(2);
            assertThat(result.data())
                    .extracting(FeedResponseDto::id)
                    .containsExactly(newerFeed.getId(), olderFeed.getId());

            log.debug("result = \n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }

        @Test
        @DisplayName("피드 목록 조회 성공 - 조회 개수 초과 시 hasNext는 true")
        void searchFeeds_success_hasNext_true_when_more_than_limit() throws JsonProcessingException {
// given
            FeedFixture firstFixture = feedFixtureBuilder.createFeedFixture();
            User user = firstFixture.user();

            for (int i = 0; i < 20; i++) {
                feedFixtureBuilder.createFeedFixture();
            }

            em.flush();
            em.clear();

            FeedSearchCondition condition = new FeedSearchCondition(
                    null,
                    FeedSortCriteria.CREATED_AT,
                    SortDirection.DESCENDING,
                    null,
                    null,
                    20,
                    null,
                    null,
                    null
            );

            // when
            FeedCursorResponseDto result = feedService.searchFeeds(condition, user.getId());

            // then
            assertThat(result.data()).hasSize(20);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.totalCount()).isEqualTo(21);

            log.debug("result = \n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        }

        @Test
        @DisplayName("[Positive] 피드 목록 조회 - 다음 커서로 이어 조회된다")
        void searchFeeds_success_with_next_cursor() throws JsonProcessingException {
// given
            FeedFixtureWithClothesDetails firstFixture = feedFixtureBuilder.createFeedFixtureWithClothesDetails();
            User user = firstFixture.user();

            for (int i = 0; i < 4; i++) {
                feedFixtureBuilder.createFeedFixtureWithClothesDetails();
            }

            em.flush();
            em.clear();

            FeedSearchCondition firstCondition = new FeedSearchCondition(
                    null,
                    FeedSortCriteria.CREATED_AT,
                    SortDirection.DESCENDING,
                    null,
                    null,
                    3,
                    null,
                    null,
                    null
            );

            // when
            FeedCursorResponseDto firstPage = feedService.searchFeeds(firstCondition, user.getId());

            FeedSearchCondition secondCondition = new FeedSearchCondition(
                    null,
                    FeedSortCriteria.CREATED_AT,
                    SortDirection.DESCENDING,
                    firstPage.nextCursor(),
                    firstPage.nextIdAfter(),
                    3,
                    null,
                    null,
                    null
            );

            FeedCursorResponseDto secondPage = feedService.searchFeeds(secondCondition, user.getId());

            // then
            assertThat(firstPage.data()).hasSize(3);
            assertThat(firstPage.hasNext()).isTrue();

            assertThat(secondPage.data()).isNotEmpty();

            Set<UUID> firstPageIds = firstPage.data().stream()
                    .map(FeedResponseDto::id)
                    .collect(Collectors.toSet());

            Set<UUID> secondPageIds = secondPage.data().stream()
                    .map(FeedResponseDto::id)
                    .collect(Collectors.toSet());

            assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);

            log.debug("firstPage = \n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(firstPage));
            log.debug("secondPage = \n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(secondPage));
        }

    }



}