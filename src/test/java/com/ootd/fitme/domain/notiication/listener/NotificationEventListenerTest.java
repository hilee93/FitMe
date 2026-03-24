package com.ootd.fitme.domain.notiication.listener;

import com.ootd.fitme.domain.notiication.event.*;
import com.ootd.fitme.domain.notiication.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Nested
    @DisplayName("이벤트 수신 시 알림 서비스 호출")
    class EventHandlingTest {

        @Test
        @DisplayName("DM 이벤트 -> notifyDirectMessage 호출")
        void directMessage() {
            UUID userId = UUID.randomUUID();

            DirectMessageReceivedEvent event =
                    new DirectMessageReceivedEvent(userId, "sender", "msg");

            listener.directMessage(event);

            verify(notificationService)
                    .notifyDirectMessage(userId, "sender", "msg");
        }

        @Test
        @DisplayName("팔로우 이벤트 -> notifyFollowed 호출")
        void followed() {
            UUID userId = UUID.randomUUID();

            FollowedEvent event =
                    new FollowedEvent(userId, "follower");

            listener.followed(event);

            verify(notificationService)
                    .notifyFollowed(userId, "follower");
        }

        @Test
        @DisplayName("좋아요 이벤트 -> notifyFeedLiked 호출")
        void feedLiked() {
            UUID userId = UUID.randomUUID();

            FeedLikedEvent event =
                    new FeedLikedEvent(userId, "liker");

            listener.feedLiked(event);

            verify(notificationService)
                    .notifyFeedLiked(userId, "liker");
        }

        @Test
        @DisplayName("댓글 이벤트 -> notifyFeedCommented 호출")
        void feedCommented() {
            UUID userId = UUID.randomUUID();

            FeedCommentedEvent event =
                    new FeedCommentedEvent(userId, "commenter", "nice");

            listener.feedCommented(event);

            verify(notificationService)
                    .notifyFeedCommented(userId, "commenter", "nice");
        }

        @Test
        @DisplayName("속성 추가 이벤트 -> notifyAttributeAdded 호출")
        void attributeAdded() {
            UUID userId = UUID.randomUUID();

            AttributeAddedEvent event =
                    new AttributeAddedEvent(userId, "color");

            listener.attributeAdded(event);

            verify(notificationService)
                    .notifyAttributeAdded(userId, "color");
        }

        @Test
        @DisplayName("팔로우 피드 이벤트 -> notifyFollowerNewFeed 호출")
        void followerNewFeed() {
            UUID userId = UUID.randomUUID();

            FollowerNewFeedEvent event =
                    new FollowerNewFeedEvent(userId, "writer", "feed");

            listener.followerNewFeed(event);

            verify(notificationService)
                    .notifyFollowerNewFeed(userId, "writer", "feed");
        }

        @Test
        @DisplayName("날씨 이벤트 -> notifyWeatherAlert 호출")
        void weatherAlert() {
            UUID userId = UUID.randomUUID();

            WeatherAlertEvent event =
                    new WeatherAlertEvent(userId, "비 온다");

            listener.weatherAlert(event);

            verify(notificationService)
                    .notifyWeatherAlert(userId, "비 온다");
        }
    }
}