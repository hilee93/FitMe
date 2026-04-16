package com.ootd.fitme.domain.notification.kafka;


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
import com.ootd.fitme.domain.weatherforecast.event.WeatherAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.kafka.core.KafkaTemplate;


@ConditionalOnProperty(
        name = "app.kafka.listener.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationKafkaProducer {


    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void directMessage(DirectMessageCreateEvent event) {
        sendToKafka(event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void followed(FollowCreateEvent event) {
        sendToKafka(event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void feedLiked(FeedLikedCreateEvent event) {
        sendToKafka(event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void feedCommented(FeedCommentCreateEvent event) {
        sendToKafka(event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void attributeAdded(AttributeAddedEvent event) {
        sendToKafka(event);
    }
    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void attributeUpdated(AttributeUpdateEvent event) {
        sendToKafka(event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void attributeDeleted(AttributeDeleteEvent event) {
        sendToKafka(event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void followerNewFeed(FeedCreateEvent event) {
        sendToKafka(event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void weatherAlert(WeatherAlertEvent event) {
        sendToKafka(event);
    }

    private <T> void sendToKafka(T event) {
        try {
            String topic = "fitme.".concat(event.getClass().getSimpleName());

            kafkaTemplate.send(topic, event);

            log.info("[KAFKA] 이벤트 전송 성공. topic={}, eventType={}", topic, event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("[KAFKA] 이벤트 전송 실패. eventType={}", event.getClass().getSimpleName(), e);
            throw new RuntimeException(e);
        }
    }

}