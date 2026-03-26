package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.clothesattribute.repository.ClothesAttributeRepository;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.ClothesAttributeSelectableValue;
import com.ootd.fitme.domain.clothesattributeselectablevalue.repository.ClothesAttributeSelectableValueRepository;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.exception.FeedNotFoundException;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder;
import com.ootd.fitme.domain.feed.fixture.FeedFixtureBuilder.FeedFixture;
import com.ootd.fitme.domain.feed.repository.FeedRepository;
import com.ootd.fitme.domain.feedclothes.entity.FeedClothes;
import com.ootd.fitme.domain.feedclothes.repository.FeedClothesRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.domain.selectablevalue.repository.SelectableValueRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

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
                    .isInstanceOf(NoSuchElementException.class);
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



}