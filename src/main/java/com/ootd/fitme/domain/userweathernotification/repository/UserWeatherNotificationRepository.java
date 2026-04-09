package com.ootd.fitme.domain.userweathernotification.repository;

import com.ootd.fitme.domain.userweathernotification.entity.UserWeatherNotification;
import com.ootd.fitme.domain.userweathernotification.enums.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserWeatherNotificationRepository extends JpaRepository<UserWeatherNotification, UUID> {
    Optional<UserWeatherNotification> findByUserIdAndNoticeType(
            UUID userId,
            NoticeType noticeType
    );
}
