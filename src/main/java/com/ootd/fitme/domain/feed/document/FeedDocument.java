package com.ootd.fitme.domain.feed.document;

import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@Document(indexName = "feeds")
@Setting(settingPath = "elasticsearch/feed-settings.json")
@NoArgsConstructor(access = PROTECTED)
@Getter
public class FeedDocument {

    @Id
    private UUID id;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Text, analyzer = "feed_content_analyzer")
    private String content;

    @Field(type = FieldType.Integer)
    private int commentCount;

    @Field(type = FieldType.Integer)
    private int likeCount;

    @Field(type = FieldType.Keyword)
    private UUID weatherForecastId;

    @Field(type = FieldType.Keyword)
    SkyStatus skyStatus;

    @Field(type = FieldType.Keyword)
    PrecipitationType precipitationType;

    @Field(type = FieldType.Keyword)
    private UUID userId;

    private FeedDocument(
            UUID id,
            Instant createdAt,
            String content,
            int commentCount,
            int likeCount,
            UUID weatherForecastId,
            SkyStatus skyStatus,
            PrecipitationType precipitationType,
            UUID userId
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.content = content;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.weatherForecastId = weatherForecastId;
        this.skyStatus = skyStatus;
        this.precipitationType = precipitationType;
        this.userId = userId;
    }

    public static FeedDocument create(
            UUID id,
            Instant createdAt,
            String content,
            int commentCount,
            int likeCount,
            UUID weatherForecastId,
            SkyStatus skyStatus,
            PrecipitationType precipitationType,
            UUID userId

    ) {
        return new FeedDocument(
                id,
                createdAt,
                content,
                commentCount,
                likeCount,
                weatherForecastId,
                skyStatus,
                precipitationType,
                userId
        );
    }


}
