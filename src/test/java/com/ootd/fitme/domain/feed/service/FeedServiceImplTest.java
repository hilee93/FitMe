package com.ootd.fitme.domain.feed.service;

import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.feed.dto.request.FeedCreateRequest;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.entity.Feed;
import com.ootd.fitme.domain.feed.repository.FeedRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.domain.weatherforecast.repository.WeatherForecastRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FeedServiceImplTest {

    @Autowired
    private FeedService feedService;

    @Autowired
    private FeedRepository feedRepository;

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

    @Nested
    @DisplayName("피드관리 - 피드생성")
    class CreateFeedTest {

        @Test
        @DisplayName("[Positive] 피드 생성 시 Feed, FeedClothes 저장 및 응답값이 올바르게 반환된다")
        void createFeed_success() {

            // given
            User user = userRepository.save(
                    User.create("email@test.com", "password")
            );

            Profile profile = profileRepository.save(
                    Profile.create("name", null, null, null, null, null, null, null, null, null, null, user)
            );

            Region region = Region.create(
                    "1234567810",
                    "경기도 남양주시 테스트읍 테스트동",
                    "경기도",
                    "남양주시",
                    "테스트읍 테스트동",
                    "",
                    0.0,
                    0.0,
                    0,
                    0
            );

            regionRepository.save(region);

            WeatherForecast weather = weatherForecastRepository.save(
                    WeatherForecast.create(
                            Instant.now(),
                            Instant.now(),
                            SkyStatus.CLEAR,
                            PrecipitationType.NONE,
                            0.0,
                            0.0,
                            0.0
                            , 0.0,
                            0.0,
                            0.0,
                            0.0,
                            0.0,
                            0.0,
                            WindStrengthWord.WEAK,
                            region
                    )
            );

            Clothes top = clothesRepository.save(
                    Clothes.create("상의", ClothesType.TOP, user)
            );

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
    }

}