package com.ootd.fitme.domain.notification.kafka;

import com.ootd.fitme.domain.attribute.event.AttributeAddedEvent;
import com.ootd.fitme.domain.attribute.event.AttributeDeleteEvent;
import com.ootd.fitme.domain.attribute.event.AttributeUpdateEvent;
import com.ootd.fitme.domain.comment.event.FeedCommentCreateEvent;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.domain.feed.event.FeedCreateEvent;
import com.ootd.fitme.domain.feedlike.event.FeedLikedCreateEvent;
import com.ootd.fitme.domain.follow.event.FollowCreateEvent;
import com.ootd.fitme.domain.notification.enums.AttributeAction;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.event.WeatherAlertEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private NotificationKafkaProducer producer;

    @Nested
    @DisplayName("Attribute 이벤트")
    class AttributeEventTest {

        @Test
        @DisplayName("AttributeAddedEvent 전송")
        void attributeAdded() {
            // given
            AttributeAddedEvent event = new AttributeAddedEvent(
                    UUID.randomUUID(),
                    "color",
                    AttributeAction.ADDED,
                    Instant.now()
            );

            // when
            producer.attributeAdded(event);

            // then
            verify(kafkaTemplate).send("fitme.AttributeAddedEvent", event);
        }

        @Test
        @DisplayName("AttributeUpdateEvent 전송")
        void attributeUpdated() {
            // given
            AttributeUpdateEvent event = new AttributeUpdateEvent(
                    UUID.randomUUID(),
                    "color",
                    AttributeAction.UPDATED,
                    Instant.now()
            );

            // when
            producer.attributeUpdated(event);

            // then
            verify(kafkaTemplate).send("fitme.AttributeUpdateEvent", event);
        }

        @Test
        @DisplayName("AttributeDeleteEvent 전송")
        void attributeDeleted() {
            // given
            AttributeDeleteEvent event = new AttributeDeleteEvent(
                    UUID.randomUUID(),
                    "color",
                    AttributeAction.REMOVED,
                    Instant.now()
            );

            // when
            producer.attributeDeleted(event);

            // then
            verify(kafkaTemplate).send("fitme.AttributeDeleteEvent", event);
        }
    }

    @Nested
    @DisplayName("기타 이벤트")
    class OtherEventTest {

        @Test
        @DisplayName("DirectMessage 이벤트 전송")
        void directMessage() {
            // given
            DirectMessageCreateEvent event = new DirectMessageCreateEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                     "senderName",
                     "senderProfileImageUrl",
                     "receiverName",
                     "receiverProfileImageUrl",
                     "message",
                    Instant.now()
            );

            // when
            producer.directMessage(event);

            // then
            verify(kafkaTemplate).send("fitme.DirectMessageCreateEvent", event);
        }

        @Test
        @DisplayName("Follow 이벤트 전송")
        void followed() {
            // given
            FollowCreateEvent event = new FollowCreateEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "followerName",
                    Instant.now()
            );

            // when
            producer.followed(event);

            // then
            verify(kafkaTemplate).send("fitme.FollowCreateEvent", event);
        }

        @Test
        @DisplayName("FeedLike 이벤트 전송")
        void feedLiked() {
            // given
            FeedLikedCreateEvent event = new FeedLikedCreateEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "좋아요 내용",
                    Instant.now()
            );

            // when
            producer.feedLiked(event);

            // then
            verify(kafkaTemplate).send("fitme.FeedLikedCreateEvent", event);
        }

        @Test
        @DisplayName("FeedComment 이벤트 전송")
        void feedCommented() {
            // given
            FeedCommentCreateEvent event = new FeedCommentCreateEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "게시글 내용",
                    UUID.randomUUID(),
                    "댓글 내용",
                    Instant.now()
            );

            // when
            producer.feedCommented(event);

            // then
            verify(kafkaTemplate).send("fitme.FeedCommentCreateEvent", event);
        }

        @Test
        @DisplayName("FeedCreate 이벤트 전송")
        void followerNewFeed() {
            // given
            FeedCreateEvent event = new FeedCreateEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "게시글 내용",
                    Instant.now(),
                    Instant.now(),
                    0,
                    0,
                    UUID.randomUUID(),
                    SkyStatus.CLEAR,
                    PrecipitationType.NONE
            );

            // when
            producer.followerNewFeed(event);

            // then
            verify(kafkaTemplate).send("fitme.FeedCreateEvent", event);
        }

        @Test
        @DisplayName("WeatherAlert 이벤트 전송")
        void weatherAlert() {
            // given
            WeatherAlertEvent event = new WeatherAlertEvent(
                    List.of(UUID.randomUUID(), UUID.randomUUID()),
                    "서울",
                    "강남구",
                    "날씨 알림 메시지",
                    Instant.now()
            );

            // when
            producer.weatherAlert(event);

            // then
            verify(kafkaTemplate).send("fitme.WeatherAlertEvent", event);
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionTest {

        @Test
        @DisplayName("Kafka 전송 실패 시 RuntimeException 발생")
        void shouldThrowExceptionWhenKafkaFails() {
            // given
            AttributeAddedEvent event = new AttributeAddedEvent(
                    UUID.randomUUID(),
                    "color",
                    AttributeAction.ADDED,
                    Instant.now()
            );

            doThrow(new RuntimeException("fail"))
                    .when(kafkaTemplate)
                    .send("fitme.AttributeAddedEvent", event);

            // when & then
            assertThatThrownBy(() -> producer.attributeAdded(event))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}