package com.ootd.fitme.domain.feed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.clothesattribute.repository.ClothesAttributeRepository;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.ClothesAttributeSelectableValue;
import com.ootd.fitme.domain.clothesattributeselectablevalue.repository.ClothesAttributeSelectableValueRepository;
import com.ootd.fitme.domain.feed.dto.response.FeedResponseDto;
import com.ootd.fitme.domain.feed.entity.Feed;
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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional
class FeedQueryServiceTest {

    @Autowired
    private FeedQueryService feedQueryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private WeatherForecastRepository weatherForecastRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private ClothesRepository clothesRepository;

    @Autowired
    private FeedClothesRepository feedClothesRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private SelectableValueRepository selectableValueRepository;

    @Autowired
    private ClothesAttributeRepository clothesAttributeRepository;

    @Autowired
    private ClothesAttributeSelectableValueRepository clothesAttributeSelectableValueRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getFeed_success() throws JsonProcessingException {
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

        WeatherForecast weatherForecast = weatherForecastRepository.save(
                WeatherForecast.create(
                        Instant.now(),
                        Instant.now(),
                        SkyStatus.CLEAR,
                        PrecipitationType.NONE,
                        0.0,
                        0.0,
                        9.34,
                        -0.70,
                        5.64,
                        17.55,
                        0.0,
                        10.0,
                        0.0,
                        WindStrengthWord.WEAK,
                        region
                )
        );

        Feed feed = feedRepository.save(
                Feed.create("테스트 피드 내용", 0, 0, weatherForecast, user)
        );

        Clothes dress = clothesRepository.save(
                Clothes.create("원피스", ClothesType.DRESS, user)
        );

        Clothes top = clothesRepository.save(
                Clothes.create("상의", ClothesType.TOP, user)
        );

        feedClothesRepository.saveAll(List.of(
                FeedClothes.create(feed, dress),
                FeedClothes.create(feed, top)
        ));

        Attribute sizeAttribute = attributeRepository.save(
                Attribute.create("사이즈")
        );

        SelectableValue s = selectableValueRepository.save(SelectableValue.create("S", sizeAttribute));
        SelectableValue m = selectableValueRepository.save(SelectableValue.create("M", sizeAttribute));
        SelectableValue l = selectableValueRepository.save(SelectableValue.create("L", sizeAttribute));
        SelectableValue free = selectableValueRepository.save(SelectableValue.create("FREE", sizeAttribute));

        ClothesAttribute topSize = clothesAttributeRepository.save(
                ClothesAttribute.create(top, sizeAttribute)
        );

        clothesAttributeSelectableValueRepository.save(
                ClothesAttributeSelectableValue.create(topSize, free)
        );

        // when
        FeedResponseDto result = feedQueryService.getFeed(feed.getId(), user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(feed.getId());
        assertThat(result.author().userId()).isEqualTo(user.getId());
        assertThat(result.author().name()).isEqualTo("name");
        assertThat(result.weather().skyStatus()).isEqualTo(SkyStatus.CLEAR);
        assertThat(result.ootds()).hasSize(2);

        assertThat(result.ootds())
                .anySatisfy(clothes -> {
                    if (clothes.type() == ClothesType.TOP) {
                        assertThat(clothes.attributes()).hasSize(1);
                        assertThat(clothes.attributes().get(0).definitionName()).isEqualTo("사이즈");
                        assertThat(clothes.attributes().get(0).value()).isEqualTo("FREE");
                        assertThat(clothes.attributes().get(0).selectableValues())
                                .contains("S", "M", "L", "FREE");
                    }
                });

        log.debug("FeedResponseDto = {}", result);

        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(result);

        log.debug("FeedResponseDto JSON:\n{}", prettyJson);
    }
}