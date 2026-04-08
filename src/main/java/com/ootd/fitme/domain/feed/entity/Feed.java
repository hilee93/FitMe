package com.ootd.fitme.domain.feed.entity;

import com.ootd.fitme.domain.base.BaseUpdateEntity;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "feeds")
public class Feed extends BaseUpdateEntity {

    @Column(name = "content", nullable = false, length = 300)
    private String content;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @JoinColumn(name = "weather_forecast_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private WeatherForecast weatherForecast;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    private Feed(
            String content,
            int commentCount,
            int likeCount,
            WeatherForecast weatherForecast,
            User user
    ) {
        this.content = content;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.weatherForecast = weatherForecast;
        this.user = user;
    }

    public static Feed create(
            String content,
            int commentCount,
            int likeCount,
            WeatherForecast weatherForecast,
            User user
    ) {
        return new Feed(
                content,
                commentCount,
                likeCount,
                weatherForecast,
                user
        );
    }

    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content는 비어 있을 수 없습니다.");
        }
        this.content = content;
    }
}
