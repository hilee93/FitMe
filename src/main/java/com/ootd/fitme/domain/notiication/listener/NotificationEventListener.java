package com.ootd.fitme.domain.notiication.listener;


import com.ootd.fitme.domain.attribute.event.AttributeAddedEvent;
import com.ootd.fitme.domain.comment.event.FeedCommentCreateEvent;
import com.ootd.fitme.domain.directmessage.event.DirectMessageCreateEvent;
import com.ootd.fitme.domain.feed.event.FeedCreateEvent;
import com.ootd.fitme.domain.feedlike.event.FeedLikedCreateEvent;
import com.ootd.fitme.domain.follow.event.FollowCreateEvent;
import com.ootd.fitme.domain.notiication.service.NotificationService;
import com.ootd.fitme.domain.weatherforecast.event.WeatherAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void directMessage(DirectMessageCreateEvent event) {

        try {
            notificationService.notifyDirectMessage(
                    event.receiverId(),
                    event.senderName(),
                    event.message()
            );
            log.info("[DM] 알림이벤트 처리 성공. receiverId={}", event.receiverId());
        } catch (Exception e) {
            log.error("[DM] 알림이벤트 처리 실패. receiverId={}", event.receiverId(), e);
        }
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void followed(FollowCreateEvent event) {
        try {
            notificationService.notifyFollowed(
                    event.userId(),
                    event.followerName()
            );
            log.info("[FOLLOW] 알림이벤트 처리 성공. userId={}", event.userId());
        } catch (Exception e) {
            log.error("[FOLLOW] 알림이벤트 처리 실패. userId={}", event.userId(), e);
        }
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void feedLiked(FeedLikedCreateEvent event) {
        try {
            notificationService.notifyFeedLiked(
                    event.userId(),
                    event.likerName()
            );
            log.info("[LIKE] 알림이벤트 처리 성공. userId={}", event.userId());
        } catch (Exception e) {
            log.error("[LIKE] 알림이벤트 처리 실패. userId={}", event.userId(), e);
        }
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void feedCommented(FeedCommentCreateEvent event) {
        try {
            notificationService.notifyFeedCommented(
                    event.userId(),
                    event.commenterName(),
                    event.comment()
            );
            log.info("[COMMENT] 알림이벤트 처리 성공. userId={}", event.userId());
        } catch (Exception e) {
            log.error("[COMMENT] 알림이벤트 처리 실패. userId={}", event.userId(), e);
        }
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void attributeAdded(AttributeAddedEvent event) {
        try {
            notificationService.notifyAttributeAdded(
                    event.attributeName()
            );
            log.info("[ATTRIBUTE_ADDED] 알림이벤트 처리 성공.");
        } catch (Exception e) {
            log.error("[ATTRIBUTE_ADDED] 알림이벤트 처리 실패.", e);
        }
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void followerNewFeed(FeedCreateEvent event) {
        try {
            notificationService.notifyFollowerNewFeed(
                    event.userId(),
                    event.writerName(),
                    event.feedName()
            );
            log.info("[FOLLOWER_FEED] 알림이벤트 처리 성공. userId={}", event.userId());
        } catch (Exception e) {
            log.error("[FOLLOWER_FEED] 알림이벤트 처리 실패. userId={}", event.userId(), e);
        }
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void weatherAlert(WeatherAlertEvent event) {
        try {
            notificationService.notifyWeatherAlert(
                    event.message()
            );
            log.info("[WEATHER_ALERT] 알림이벤트 처리 성공.");
        } catch (Exception e) {
            log.error("[WEATHER_ALERT] 알림이벤트 처리 실패.", e);
        }
    }
}
