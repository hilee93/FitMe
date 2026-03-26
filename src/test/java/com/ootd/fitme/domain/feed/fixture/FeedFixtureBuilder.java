package com.ootd.fitme.domain.feed.fixture;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.attribute.repository.AttributeRepository;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.repository.ClothesRepository;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.clothesattribute.repository.ClothesAttributeRepository;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.ClothesAttributeSelectableValue;
import com.ootd.fitme.domain.clothesattributeselectablevalue.repository.ClothesAttributeSelectableValueRepository;
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
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class FeedFixtureBuilder {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RegionRepository regionRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final FeedRepository feedRepository;
    private final ClothesRepository clothesRepository;
    private final FeedClothesRepository feedClothesRepository;
    private final AttributeRepository attributeRepository;
    private final SelectableValueRepository selectableValueRepository;
    private final ClothesAttributeRepository clothesAttributeRepository;
    private final ClothesAttributeSelectableValueRepository clothesAttributeSelectableValueRepository;


    public FeedFixtureBuilder(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            RegionRepository regionRepository,
            WeatherForecastRepository weatherForecastRepository,
            FeedRepository feedRepository,
            ClothesRepository clothesRepository,
            FeedClothesRepository feedClothesRepository,
            AttributeRepository attributeRepository,
            SelectableValueRepository selectableValueRepository,
            ClothesAttributeRepository clothesAttributeRepository,
            ClothesAttributeSelectableValueRepository clothesAttributeSelectableValueRepository
    ){
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.regionRepository = regionRepository;
        this.weatherForecastRepository = weatherForecastRepository;
        this.feedRepository = feedRepository;
        this.clothesRepository = clothesRepository;
        this.feedClothesRepository = feedClothesRepository;
        this.attributeRepository = attributeRepository;
        this.selectableValueRepository = selectableValueRepository;
        this.clothesAttributeRepository = clothesAttributeRepository;
        this.clothesAttributeSelectableValueRepository = clothesAttributeSelectableValueRepository;
    }


    public FeedFixture createFeedFixture() {
        User user = userRepository.save(
                User.create("email-" + UUID.randomUUID() + "@test.com", "password")
        );

        profileRepository.save(
                Profile.create("name", null, null, null, null, null, null, null, null, null, null, user)
        );

        Region region = regionRepository.save(
                Region.create(
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
                )
        );

        WeatherForecast weather = weatherForecastRepository.save(
                WeatherForecast.create(
                        Instant.now(),
                        Instant.now(),
                        SkyStatus.CLEAR,
                        PrecipitationType.NONE,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        WindStrengthWord.WEAK,
                        region
                )
        );

        Feed feed = feedRepository.save(
                Feed.create("테스트 피드", 0, 0, weather, user)
        );

        Clothes clothes = clothesRepository.save(
                Clothes.create("상의", ClothesType.TOP, user)
        );

        FeedClothes feedClothes = feedClothesRepository.save(
                FeedClothes.create(feed, clothes)
        );

        return new FeedFixture(user, region, weather, feed, clothes, feedClothes);
    }

    public FeedFixtureWithClothesDetails createFeedFixtureWithClothesDetails() {
        User user = userRepository.save(
                User.create("email2@test.com", "password")
        );

        profileRepository.save(
                Profile.create("name", null, null, null, null, null, null, null, null, null, null, user)
        );

        Region region = regionRepository.save(
                Region.create(
                        "2234567810",
                        "경기도 남양주시 테스트읍 상세동",
                        "경기도",
                        "남양주시",
                        "테스트읍 상세동",
                        "",
                        0.0,
                        0.0,
                        0,
                        0
                )
        );

        WeatherForecast weather = weatherForecastRepository.save(
                WeatherForecast.create(
                        Instant.now(),
                        Instant.now(),
                        SkyStatus.CLEAR,
                        PrecipitationType.NONE,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        WindStrengthWord.WEAK,
                        region
                )
        );

        Feed feed = feedRepository.save(
                Feed.create("상세 피드", 0, 0, weather, user)
        );

        Clothes clothes = clothesRepository.save(
                Clothes.create("상의", ClothesType.TOP, user)
        );

        FeedClothes feedClothes = feedClothesRepository.save(
                FeedClothes.create(feed, clothes)
        );

        Attribute sizeAttribute = attributeRepository.save(
                Attribute.create("사이즈")
        );

        SelectableValue s = selectableValueRepository.save(
                SelectableValue.create("S", sizeAttribute)
        );
        SelectableValue m = selectableValueRepository.save(
                SelectableValue.create("M", sizeAttribute)
        );
        SelectableValue l = selectableValueRepository.save(
                SelectableValue.create("L", sizeAttribute)
        );

        SelectableValue free = selectableValueRepository.save(
                SelectableValue.create("FREE", sizeAttribute)
        );

        ClothesAttribute clothesAttribute = clothesAttributeRepository.save(
                ClothesAttribute.create(clothes, sizeAttribute)
        );

        ClothesAttributeSelectableValue selectedValue = clothesAttributeSelectableValueRepository.save(
                ClothesAttributeSelectableValue.create(clothesAttribute, free)
        );

        return new FeedFixtureWithClothesDetails(
                user,
                region,
                weather,
                feed,
                clothes,
                feedClothes,
                sizeAttribute,
                List.of(s, m, l, free),
                clothesAttribute,
                selectedValue
        );
    }

    public record FeedFixture(
            User user,
            Region region,
            WeatherForecast weather,
            Feed feed,
            Clothes clothes,
            FeedClothes feedClothes
    ) {
    }

    public record FeedFixtureWithClothesDetails(
            User user,
            Region region,
            WeatherForecast weather,
            Feed feed,
            Clothes clothes,
            FeedClothes feedClothes,
            Attribute attribute,
            List<SelectableValue> selectableValues,
            ClothesAttribute clothesAttribute,
            ClothesAttributeSelectableValue selectedValue
    ) {
    }
}
