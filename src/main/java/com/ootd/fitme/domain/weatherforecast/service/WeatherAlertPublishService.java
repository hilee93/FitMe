package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.notification.repository.NotificationProfileRepository;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.userweathernotification.entity.UserWeatherNotification;
import com.ootd.fitme.domain.userweathernotification.enums.NoticeType;
import com.ootd.fitme.domain.userweathernotification.repository.UserWeatherNotificationRepository;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.event.WeatherAlertEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WeatherAlertPublishService {
    private static final Duration DEDUPE_WINDOW = Duration.ofMinutes(30);
    private static final double TEMP_SWING_THRESHOLD = 15.0;
    private static final double COLD_WAVE_THRESHOLD = -12.0;
    private static final double HEAT_WAVE_THRESHOLD = 33.0;

    private final UserWeatherNotificationRepository userWeatherNotificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationProfileRepository notificationProfileRepository;

    public void publishIfNeeded(
            Region region,
            PrecipitationType previousPrecipitationType,
            WeatherForecast currentForecast
    ) {
        if (region == null || currentForecast == null) {
            return;
        }

        if (isNewPrecipitationDetected(previousPrecipitationType, currentForecast.getPrecipitationType())) {
            publish(region, NoticeType.PRECIPITATION_START, "강수 예보 감지");
        }

        if (isTemperatureSwing(currentForecast)) {
            publish(region, NoticeType.TEMPERATURE_SWING, "급격한 기온 변화 감지");
        }

        if (isColdOrHeat(currentForecast)) {
            publish(region, NoticeType.COLD_HEAT, "한파/폭염 주의");
        }
    }

    private void publish(Region region, NoticeType noticeType, String message) {
        List<UUID> candidates = notificationProfileRepository
                .findUsersByRegion1AndRegion2(region.getRegion1depthName(), region.getRegion2depthName())
                .stream()
                .map(User::getId)
                .distinct()
                .toList();

        if (candidates.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        Instant dedupeSince = now.minus(DEDUPE_WINDOW);

        List<UUID> receivers = new ArrayList<>();
        List<UserWeatherNotification> newMarkers = new ArrayList<>();

        for (UUID userId : candidates) {
            Optional<UserWeatherNotification> markerOpt = userWeatherNotificationRepository
                    .findByUserIdAndNoticeType(userId, noticeType);

            if (markerOpt.isPresent()) {
                UserWeatherNotification marker = markerOpt.get();

                if (marker.getSentAt() != null && !marker.getSentAt().isBefore(dedupeSince)) {
                    continue;
                }

                marker.updateSentAt(now);
                receivers.add(userId);
                continue;
            }

            newMarkers.add(UserWeatherNotification.create(noticeType, now, userId));
            receivers.add(userId);
        }

        if (receivers.isEmpty()) {
            return;
        }

        if (!newMarkers.isEmpty()) {
            userWeatherNotificationRepository.saveAll(newMarkers);
        }


        eventPublisher.publishEvent(new WeatherAlertEvent(
                receivers,
                region.getRegion1depthName(),
                region.getRegion2depthName(),
                message,
                now
        ));
    }

    private boolean isNewPrecipitationDetected(PrecipitationType previous, PrecipitationType current) {
        if (previous == null || current == null) {
            return false;
        }
        return isDry(previous) && isWet(current);
    }

    private boolean isDry(PrecipitationType type) {
        return type == PrecipitationType.NONE;
    }

    private boolean isWet(PrecipitationType type) {
        return type == PrecipitationType.SHOWER
                || type == PrecipitationType.RAIN
                || type == PrecipitationType.SNOW
                || type == PrecipitationType.RAIN_SNOW;
    }

    private boolean isTemperatureSwing(WeatherForecast current) {
        Double diff = current.getTemperatureComparedToDayBefore();
        return diff != null && Math.abs(diff) >= TEMP_SWING_THRESHOLD;
    }

    private boolean isColdOrHeat(WeatherForecast current) {
        Double min = current.getTemperatureMin();
        Double max = current.getTemperatureMax();
        return (min != null && min <= COLD_WAVE_THRESHOLD)
                || (max != null && max >= HEAT_WAVE_THRESHOLD);
    }
}
