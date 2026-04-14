package com.ootd.fitme.domain.notification.listener;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ootd.fitme.domain.attribute.event.AttributeAddedEvent;
import com.ootd.fitme.domain.attribute.event.AttributeDeleteEvent;
import com.ootd.fitme.domain.attribute.event.AttributeUpdateEvent;
import com.ootd.fitme.domain.comment.event.FeedCommentCreateEvent;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.domain.feed.event.FeedCreateEvent;
import com.ootd.fitme.domain.feedlike.event.FeedLikedCreateEvent;
import com.ootd.fitme.domain.follow.event.FollowCreateEvent;
import com.ootd.fitme.domain.notification.service.NotificationService;
import com.ootd.fitme.domain.weatherforecast.event.WeatherAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "fitme.DirectMessageCreateEvent", groupId = "notification-save-group")
    public void directMessage(DirectMessageCreateEvent  event) {
        try {

            notificationService.notifyDirectMessage(
                    event.receiverId(),
                    event.senderName(),
                    event.message()
            );

            log.info("[KAFKA][DM] 처리 성공 messageId={}", event.messageId());

        } catch (Exception e) {
            log.error("[KAFKA][DM] 알림 서비스 처리 실패", e);
        }
    }

    @KafkaListener(topics = "fitme.FollowCreateEvent", groupId = "notification-save-group")
    public void followed(FollowCreateEvent event) {
        try {
            notificationService.notifyFollowed(
                    event.followeeId(),
                    event.followerName()
            );

            log.info("[KAFKA][FOLLOW] 처리 성공 followId={}", event.followId());
        } catch (Exception e) {
            log.error("[KAFKA][FOLLOW] 알림 서비스 처리 실패 followId={}", event.followId(), e);
        }
    }

    @KafkaListener(topics = "fitme.FeedLikedCreateEvent", groupId = "notification-save-group")
    public void feedLiked(FeedLikedCreateEvent event) {
        try {
            notificationService.notifyFeedLiked(
                    event.targetUserId(),
                    event.content(),
                    event.likerId()
            );

            log.info("[KAFKA][LIKE] 처리 성공 likeId={}", event.feedLikeId());
        } catch (Exception e) {
            log.error("[KAFKA][LIKE] 알림 서비스 처리 실패 likeId={}", event.feedLikeId(), e);
        }
    }

    @KafkaListener(topics = "fitme.FeedCommentCreateEvent", groupId = "notification-save-group")
    public void feedCommented(FeedCommentCreateEvent event) {
        try {
            notificationService.notifyFeedCommented(
                    event.feedOwnerId(),
                    event.content(),
                    event.commenterId(),
                    event.comment()
            );

            log.info("[KAFKA][COMMENT] 처리 성공 commentId={}", event.commentId());
        } catch (Exception e) {
            log.error("[KAFKA][COMMENT] 알림 서비스 처리 실패 commentId={}", event.commentId(), e);
        }
    }

    @KafkaListener(topics = "fitme.AttributeAddedEvent", groupId = "notification-save-group")
    public void attributeAdded(AttributeAddedEvent event) {
        try {
            notificationService.notifyAttributeAdded(
                    event.attributeName(),
                    event.action()
            );

            log.info("[KAFKA][ATTRIBUTE_ADDED] 처리 성공 attributeId={}", event.attributeId());
        } catch (Exception e) {
            log.error("[KAFKA][ATTRIBUTE_ADDED] 알림 서비스 처리 실패 attributeId={}", event.attributeId(), e);
        }
    }

    @KafkaListener(topics = "fitme.FeedCreateEvent", groupId = "notification-save-group")
    public void followerNewFeed(FeedCreateEvent event) {
        try {
            notificationService.notifyFollowerNewFeed(
                    event.userId(),
                    event.content()
            );

            log.info("[KAFKA][FOLLOWER_FEED] 처리 성공 feedId={}", event.feedId());
        } catch (Exception e) {
            log.error("[KAFKA][FOLLOWER_FEED] 알림 서비스 처리 실패 feedId={}", event.feedId(), e);
        }
    }

    @KafkaListener(topics = "fitme.WeatherAlertEvent", groupId = "notification-save-group")
    public void weatherAlert(WeatherAlertEvent event) {
        try {
            notificationService.notifyWeatherAlert(
                    event.receiverIds(),
                    event.region1DepthName(),
                    event.region2DepthName(),
                    event.message()
            );

            log.info("[KAFKA][WEATHER_ALERT] 처리 성공");
        } catch (Exception e) {
            log.error("[KAFKA][WEATHER_ALERT] 알림 서비스 처리 실패", e);
        }
    }
}
