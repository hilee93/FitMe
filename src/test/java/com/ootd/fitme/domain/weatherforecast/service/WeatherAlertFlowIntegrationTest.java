package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.notification.service.NotificationService;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.region.service.RegionService;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.userweathernotification.entity.UserWeatherNotification;
import com.ootd.fitme.domain.userweathernotification.enums.NoticeType;
import com.ootd.fitme.domain.userweathernotification.repository.UserWeatherNotificationRepository;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherDto;
import com.ootd.fitme.infrastructure.external.openweather.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = {
        "spring.batch.job.enabled=false",
})
@Transactional
public class WeatherAlertFlowIntegrationTest {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired
    private WeatherForecastService weatherForecastService;

    @Autowired
    private WeatherForecastCollectService weatherForecastCollectService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserWeatherNotificationRepository userWeatherNotificationRepository;

    @Autowired
    private EntityManager em;

    @MockitoBean
    private OpenWeatherClient openWeatherClient;

    @MockitoBean
    private RegionService regionService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @DisplayName("fallback 수집 후 즉시 배치 수집은 30분 내 중복 방지")
    void fallback_then_batch_within30minutes_deduped() {
        Region region = saveRegion();
        User user = saveUserWithProfile(region);

        given(regionService.resolveAndUpsert(anyDouble(), anyDouble())).willReturn(region);
        given(openWeatherClient.fetch5Day3HourForecast(anyDouble(), anyDouble()))
                .willReturn(coldWaveForecastItems(7));

        List<WeatherDto> result = weatherForecastService.getWeathers(126.9707, 37.5841);
        assertThat(result).isNotEmpty();

        em.flush();
        em.clear();

        UserWeatherNotification firstMarker = userWeatherNotificationRepository
                .findByUserIdAndNoticeType(user.getId(), NoticeType.COLD_HEAT)
                .orElseThrow();

        Instant firstSentAt = firstMarker.getSentAt();

        weatherForecastCollectService.collectAndStoreAllRegions();
        em.flush();
        em.clear();

        UserWeatherNotification afterBatch = userWeatherNotificationRepository
                .findByUserIdAndNoticeType(user.getId(), NoticeType.COLD_HEAT)
                .orElseThrow();

        assertThat(afterBatch.getSentAt()).isEqualTo(firstSentAt);
        assertThat(userWeatherNotificationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("30분 경과 후 배치 수집은 sentAt을 갱신하며 재발행 가능")
    void batch_after30minutes_updatesSentAt() {
        Region region = saveRegion();
        User user = saveUserWithProfile(region);

        given(openWeatherClient.fetch5Day3HourForecast(anyDouble(), anyDouble()))
                .willReturn(coldWaveForecastItems(7));

        Instant oldSentAt = Instant.now().minusSeconds(31 * 60);
        userWeatherNotificationRepository.save(
                UserWeatherNotification.create(NoticeType.COLD_HEAT, oldSentAt, user.getId())
        );

        weatherForecastCollectService.collectAndStoreAllRegions();
        em.flush();
        em.clear();

        UserWeatherNotification refreshed = userWeatherNotificationRepository
                .findByUserIdAndNoticeType(user.getId(), NoticeType.COLD_HEAT)
                .orElseThrow();

        assertThat(refreshed.getSentAt()).isAfter(oldSentAt);
        assertThat(userWeatherNotificationRepository.findAll()).hasSize(1);
    }

    private Region saveRegion() {
        return regionRepository.save(
                Region.create(
                        "1168010100",
                        "서울특별시 강남구 역삼동",
                        "서울특별시",
                        "강남구",
                        "역삼동",
                        null,
                        127.033,
                        37.497,
                        127,
                        37
                )
        );
    }

    private User saveUserWithProfile(Region region) {
        String email = UUID.randomUUID() + "@fitme.com";
        User user = userRepository.save(User.create(email, "encoded-password"));

        profileRepository.save(Profile.create(
                "tester",
                region.getLongitude(),
                region.getLatitude(),
                region.getX(),
                region.getY(),
                region.getRegion1depthName(),
                region.getRegion2depthName(),
                region.getRegion3depthName(),
                null,
                null,
                null,
                user
        ));
        return user;
    }

    private List<ForecastItem> coldWaveForecastItems(int dayCount) {
        LocalDate today = LocalDate.now(KST);

        return IntStream.range(0, dayCount)
                .mapToObj(i -> {
                    long epoch = today.plusDays(i).atTime(12, 0).atZone(KST).toEpochSecond();
                    return new ForecastItem(
                            epoch,
                            new Main(5.0, -15.0, 7.0, 60.0),
                            List.of(new Weather(800, "Clear", "clear sky")),
                            new Wind(2.0),
                            0.0,
                            new Rain(0.0),
                            new Snow(0.0)
                    );
                })
                .toList();
    }
}
