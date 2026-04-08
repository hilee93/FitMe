package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.notification.repository.NotificationProfileRepository;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.userweathernotification.entity.UserWeatherNotification;
import com.ootd.fitme.domain.userweathernotification.enums.NoticeType;
import com.ootd.fitme.domain.userweathernotification.repository.UserWeatherNotificationRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.domain.weatherforecast.event.WeatherAlertEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class WeatherAlertPublishServiceUnitTest {
    @Mock
    private UserWeatherNotificationRepository userWeatherNotificationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private NotificationProfileRepository notificationProfileRepository;

    @InjectMocks
    private WeatherAlertPublishService weatherAlertPublishService;

    @Test
    @DisplayName("강수 시작: 신규 marker 저장 후 이벤트 발행")
    void publishIfNeeded_precipitationStrat_newMarker() {
        Region region = mockRegion("서울", "강남구");
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        given(notificationProfileRepository.findUsersByRegion1AndRegion2("서울", "강남구"))
                .willReturn(List.of(user));

        Instant sentAt = Instant.now().minusSeconds(10 * 60);
        UserWeatherNotification marker = UserWeatherNotification.create(
                NoticeType.PRECIPITATION_START, sentAt, userId
        );

        given(userWeatherNotificationRepository.findByUserIdAndNoticeType(userId, NoticeType.PRECIPITATION_START))
                .willReturn(Optional.of(marker));

        WeatherForecast current = weatherForecast(PrecipitationType.RAIN, 0.0, 10.0, 20.0);

        weatherAlertPublishService.publishIfNeeded(region, PrecipitationType.NONE, current);

        then(userWeatherNotificationRepository).should(never()).saveAll(any());
        then(eventPublisher).should(never()).publishEvent(any());
        assertThat(marker.getSentAt()).isEqualTo(sentAt);
    }

    @Test
    @DisplayName("30분 경과 후 동일 타입은 sentAt 갱신 후 재발행")
    void publishIfNeeded_after30Minutes_republish() {
        Region region = mockRegion("서울", "강남구");
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        given(notificationProfileRepository.findUsersByRegion1AndRegion2("서울", "강남구"))
                .willReturn(List.of(user));

        Instant oldSentAt = Instant.now().minusSeconds(31 * 60);
        UserWeatherNotification marker = UserWeatherNotification.create(
                NoticeType.PRECIPITATION_START, oldSentAt, userId
        );

        given(userWeatherNotificationRepository.findByUserIdAndNoticeType(userId, NoticeType.PRECIPITATION_START))
                .willReturn(Optional.of(marker));

        WeatherForecast current = weatherForecast(PrecipitationType.RAIN, 0.0, 10.0, 20.0);

        weatherAlertPublishService.publishIfNeeded(region, PrecipitationType.NONE, current);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        then(eventPublisher).should().publishEvent(eventCaptor.capture());

        WeatherAlertEvent event = (WeatherAlertEvent) eventCaptor.getValue();

        assertThat(event.receiverIds()).containsExactly(userId);
        assertThat(event.message()).isEqualTo("강수 예보 감지");
        assertThat(marker.getSentAt()).isAfter(oldSentAt);
    }

    @Test
    @DisplayName("기온 급변 조건이면 TEMPERATURE_SWING 타입으로 발행")
    void publishIfNeeded_temperatureSwing() {
        Region region = mockRegion("서울", "강남구");
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        given(notificationProfileRepository.findUsersByRegion1AndRegion2("서울", "강남구"))
                .willReturn(List.of(user));
        given(userWeatherNotificationRepository.findByUserIdAndNoticeType(userId, NoticeType.TEMPERATURE_SWING))
                .willReturn(Optional.empty());

        WeatherForecast current = weatherForecast(PrecipitationType.NONE, 16.0, 10.0, 20.0);

        weatherAlertPublishService.publishIfNeeded(region, PrecipitationType.NONE, current);

        then(userWeatherNotificationRepository).should().findByUserIdAndNoticeType(userId, NoticeType.TEMPERATURE_SWING);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        then(eventPublisher).should().publishEvent(eventCaptor.capture());

        WeatherAlertEvent event = (WeatherAlertEvent) eventCaptor.getValue();
        assertThat(event.message()).isEqualTo("급격한 기온 변화 감지");
    }

    private Region mockRegion(String region1, String region2) {
        Region region = mock(Region.class);
        given(region.getRegion1depthName()).willReturn(region1);
        given(region.getRegion2depthName()).willReturn(region2);
        return region;
    }

    private WeatherForecast weatherForecast(
            PrecipitationType precipitationType,
            double tempDiff,
            double min,
            double max
    ) {
        Region dummyRegion = mock(Region.class);

        return WeatherForecast.create(
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600),
                SkyStatus.CLEAR,
                precipitationType,
                0.0,
                0.0,
                50.0,
                0.0,
                20.0,
                tempDiff,
                min,
                max,
                3.0,
                WindStrengthWord.WEAK,
                dummyRegion
        );
    }
}
