package com.ootd.fitme.domain.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.attribute.event.AttributeAddedEvent;
import com.ootd.fitme.domain.attribute.event.AttributeDeleteEvent;
import com.ootd.fitme.domain.attribute.event.AttributeUpdateEvent;
import com.ootd.fitme.domain.comment.event.FeedCommentCreateEvent;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.domain.feed.event.FeedCreateEvent;
import com.ootd.fitme.domain.feedlike.event.FeedLikedCreateEvent;
import com.ootd.fitme.domain.follow.event.FollowCreateEvent;
import com.ootd.fitme.domain.notification.enums.AttributeAction;
import com.ootd.fitme.domain.notification.service.NotificationService;
import com.ootd.fitme.domain.weatherforecast.event.WeatherAlertEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerUnitTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationEventListener listener;

    @Nested
    @DisplayName("이벤트 수신 시 알림 서비스 호출")
    class EventHandlingTest {

        @Test
        @DisplayName("DM 이벤트 -> notifyDirectMessage 호출")
        void directMessage() {
            UUID userId = UUID.randomUUID();
            UUID senderId = UUID.randomUUID();
            Instant now = Instant.now();

            DirectMessageCreateEvent event =
                    new DirectMessageCreateEvent(null, userId, senderId, "sender",
                            null, null, null, "msg", now);

            listener.directMessage(event);

            verify(notificationService)
                    .notifyDirectMessage(userId, "sender", "msg");
        }

        @Test
        @DisplayName("팔로우 이벤트 -> notifyFollowed 호출")
        void followed() {
            UUID followedId = UUID.randomUUID();
            UUID followeeId = UUID.randomUUID();
            UUID followerId = UUID.randomUUID();

            FollowCreateEvent event =
                    new FollowCreateEvent(followedId, followeeId, followerId, "follower", Instant.now());

            listener.followed(event);

            verify(notificationService)
                    .notifyFollowed(followeeId, "follower");
        }

        @Test
        @DisplayName("좋아요 이벤트 -> notifyFeedLiked 호출")
        void feedLiked() {
            UUID feedLikeId = UUID.randomUUID();
            UUID feedId = UUID.randomUUID();
            UUID targetUserId = UUID.randomUUID();
            UUID likerId = UUID.randomUUID();
            Instant now = Instant.now();

            FeedLikedCreateEvent event =
                    new FeedLikedCreateEvent(feedLikeId, feedId, targetUserId, likerId, "content", now);

            listener.feedLiked(event);

            verify(notificationService)
                    .notifyFeedLiked(targetUserId, "content", likerId);
        }

        @Test
        @DisplayName("댓글 이벤트 -> notifyFeedCommented 호출")
        void feedCommented() {
            UUID commentId = UUID.randomUUID();
            UUID feedId = UUID.randomUUID();
            UUID feedOwnerId = UUID.randomUUID();
            UUID commenterId = UUID.randomUUID();
            Instant now = Instant.now();

            FeedCommentCreateEvent event =
                    new FeedCommentCreateEvent(
                            commentId,
                            feedId,
                            feedOwnerId,
                            "content",
                            commenterId,
                            "comment",
                            now
                    );

            listener.feedCommented(event);

            verify(notificationService)
                    .notifyFeedCommented(feedOwnerId, "content", commenterId, "comment");
        }

        @Test
        @DisplayName("속성 추가 이벤트 -> notifyAttributeAdded 호출")
        void attributeAdded() {
            UUID attributeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            AttributeAddedEvent event =
                    new AttributeAddedEvent(attributeId, "color", AttributeAction.ADDED, Instant.now());

            listener.attributeAdded(event);

            verify(notificationService)
                    .notifyAttributeAdded("color", AttributeAction.ADDED);
        }

        @Test
        @DisplayName("속성 수정 이벤트 -> notifyAttributeAdded 호출")
        void attributeUpdated() {
            // given
            UUID attributeId = UUID.randomUUID();

            AttributeUpdateEvent event =
                    new AttributeUpdateEvent(
                            attributeId,
                            "color",
                            AttributeAction.UPDATED,
                            Instant.now()
                    );

            // when
            listener.attributeUpdated(event);

            // then
            verify(notificationService)
                    .notifyAttributeAdded("color", AttributeAction.UPDATED);
        }

        @Test
        @DisplayName("속성 삭제 이벤트 -> notifyAttributeAdded 호출")
        void attributeDeleted() {
            // given
            UUID attributeId = UUID.randomUUID();

            AttributeDeleteEvent event =
                    new AttributeDeleteEvent(
                            attributeId,
                            "color",
                            AttributeAction.REMOVED,
                            Instant.now()
                    );

            // when
            listener.attributeDeleted(event);

            // then
            verify(notificationService)
                    .notifyAttributeAdded("color", AttributeAction.REMOVED);
        }

        @Test
        @DisplayName("팔로우 피드 이벤트 -> notifyFollowerNewFeed 호출")
        void followerNewFeed() {
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FeedCreateEvent event =
                    new FeedCreateEvent(
                            feedId,
                            userId,
                            "content",
                            Instant.now(),
                            Instant.now(),
                            0,
                            0,
                            null,
                            null,
                            null
                            );

            listener.followerNewFeed(event);

            verify(notificationService)
                    .notifyFollowerNewFeed(userId, "content");
        }

        @Test
        @DisplayName("날씨 이벤트 -> notifyWeatherAlert 호출")
        void weatherAlert() {

            List<UUID> receiverIds = List.of(UUID.randomUUID(), UUID.randomUUID());

            WeatherAlertEvent event =
                    new WeatherAlertEvent(receiverIds, "지역1", "지역2", "비 온다", Instant.now());

            listener.weatherAlert(event);

            verify(notificationService)
                    .notifyWeatherAlert(receiverIds, "지역1", "지역2", "비 온다");
        }
    }
}