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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FeedQueryServiceTest {

    private static final Logger log = LoggerFactory.getLogger(FeedQueryServiceTest.class);

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
    @Autowired
    private FeedFixtureBuilder feedFixtureBuilder;

    @Nested
    class GetFeedTest {

        @Test
        @DisplayName("[Positive] 피드 조회 - 피드 단건 조회 시 연관된 작성자/날씨/의상/속성 정보가 올바르게 조합되어 반환된다 ")
        void getFeed_success() throws JsonProcessingException {
            // given
            FeedFixture feedFixture = feedFixtureBuilder.createFeedFixture();
            Feed feed = feedFixture.feed();
            User user = feedFixture.user();
            Clothes top = feedFixture.clothes();


            // NOTE: 아래는 fixture 이후 더추가하고싶은 given 추가

            Clothes dress = clothesRepository.save(
                    Clothes.create("원피스", ClothesType.DRESS, user)
            );

            feedClothesRepository.saveAll(List.of(
                    FeedClothes.create(feed, dress)
            ));

            Attribute sizeAttribute = attributeRepository.save(
                    Attribute.create("사이즈")
            );

            SelectableValue s = selectableValueRepository.save(SelectableValue.create("S", 0, sizeAttribute));
            SelectableValue m = selectableValueRepository.save(SelectableValue.create("M", 1, sizeAttribute));
            SelectableValue l = selectableValueRepository.save(SelectableValue.create("L", 2, sizeAttribute));
            SelectableValue free = selectableValueRepository.save(SelectableValue.create("FREE", 3, sizeAttribute));

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
                    .filteredOn(c -> c.type() == ClothesType.TOP)
                    .hasSize(1)
                    .first()
                    .satisfies(clothes -> {
                        assertThat(clothes.attributes()).hasSize(1);
                        assertThat(clothes.attributes().get(0).definitionName()).isEqualTo("사이즈");
                        assertThat(clothes.attributes().get(0).value()).isEqualTo("FREE");
                        assertThat(clothes.attributes().get(0).selectableValues())
                                .contains("S", "M", "L", "FREE"); // TODO: 추후 넣는 순서대로 순서보장되는 containExactly 변경할것
                    });

            log.debug("FeedResponseDto = {}", result);

            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);

            log.debug("FeedResponseDto JSON:\n{}", prettyJson);
        }


    }
}