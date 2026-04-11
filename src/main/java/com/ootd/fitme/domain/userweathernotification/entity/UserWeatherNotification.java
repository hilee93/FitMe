package com.ootd.fitme.domain.userweathernotification.entity;

import com.ootd.fitme.domain.base.BaseUpdateEntity;
import com.ootd.fitme.domain.userweathernotification.enums.NoticeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "user_weather_notifications")
public class UserWeatherNotification extends BaseUpdateEntity {

    @Column(name = "notice_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private UserWeatherNotification(NoticeType noticeType, Instant sentAt, UUID userId) {
        this.noticeType = noticeType;
        this.sentAt = sentAt;
        this.userId = userId;
    }

    public static UserWeatherNotification create(NoticeType noticeType, Instant sentAt, UUID userId) {
        return new UserWeatherNotification(noticeType, sentAt, userId);
    }

    public void updateSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }


}
