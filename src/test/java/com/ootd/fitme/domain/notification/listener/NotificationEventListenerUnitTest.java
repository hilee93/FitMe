package com.ootd.fitme.domain.notification.listener;

import com.ootd.fitme.domain.attribute.event.AttributeAddedEvent;
import com.ootd.fitme.domain.comment.event.FeedCommentCreateEvent;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.domain.feed.event.FeedCreateEvent;
import com.ootd.fitme.domain.feedlike.event.FeedLikedCreateEvent;
import com.ootd.fitme.domain.follow.event.FollowCreateEvent;
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
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerUnitTest {

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
            UUID senderId = UUID.randomUUID();
            Instant now = Instant.now();

            DirectMessageCreateEvent event =
                    new DirectMessageCreateEvent(null,userId,senderId, "sender","msg",now );

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
                    new FollowCreateEvent(followedId,followeeId,followerId, "follower",Instant.now());

            listener.followed(event);

            verify(notificationService)
                    .notifyFollowed(followeeId, "follower");
        }

        @Test
        @DisplayName("좋아요 이벤트 -> notifyFeedLiked 호출")
        void feedLiked() {
            UUID feedId = UUID.randomUUID();
            UUID likeId = UUID.randomUUID();
            UUID likedId = UUID.randomUUID();
            UUID likerId = UUID.randomUUID();


            FeedLikedCreateEvent event =
                    new FeedLikedCreateEvent(feedId,likeId,likedId,likerId, "liker", Instant.now());

            listener.feedLiked(event);

            verify(notificationService)
                    .notifyFeedLiked(likedId, "liker");
        }

        @Test
        @DisplayName("댓글 이벤트 -> notifyFeedCommented 호출")
        void feedCommented() {
            UUID commentId = UUID.randomUUID();
            UUID feedId = UUID.randomUUID();
            UUID feedOwnerId = UUID.randomUUID();
            UUID commenterId = UUID.randomUUID();

            FeedCommentCreateEvent event =
                    new FeedCommentCreateEvent(
                            commentId,
                            feedId,
                            feedOwnerId,
                            commenterId,
                            "commenter",
                             "nice",
                            Instant.now());

            listener.feedCommented(event);

            verify(notificationService)
                    .notifyFeedCommented(feedOwnerId, "commenter", "nice");
        }

        @Test
        @DisplayName("속성 추가 이벤트 -> notifyAttributeAdded 호출")
        void attributeAdded() {
            UUID attributeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            AttributeAddedEvent event =
                    new AttributeAddedEvent(attributeId,"color",Instant.now());

            listener.attributeAdded(event);

            verify(notificationService)
                    .notifyAttributeAdded("color");
        }

        @Test
        @DisplayName("팔로우 피드 이벤트 -> notifyFollowerNewFeed 호출")
        void followerNewFeed() {
            UUID feedId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            FeedCreateEvent event =
                    new FeedCreateEvent(feedId,userId, "writer", "feed", Instant.now());

            listener.followerNewFeed(event);

            verify(notificationService)
                    .notifyFollowerNewFeed(userId, "writer", "feed");
        }

        @Test
        @DisplayName("날씨 이벤트 -> notifyWeatherAlert 호출")
        void weatherAlert() {


            WeatherAlertEvent event =
                    new WeatherAlertEvent("비 온다",Instant.now());

            listener.weatherAlert(event);

            verify(notificationService)
                    .notifyWeatherAlert("비 온다");
        }
    }
}