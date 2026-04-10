package com.ootd.fitme.domain.notification.listener;


import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.event.NotificationCreatedEvent;
import com.ootd.fitme.domain.notification.service.NotificationSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseEventListener {

    private final NotificationSseService notificationSseService;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreatedEvent event) {

        NotificationDto dto = new NotificationDto(
                event.notificationId(),
                event.createdAt(),
                event.receiverId(),
                event.title(),
                event.content(),
                event.level()
        );
        notificationSseService.send(event.receiverId(), dto);
    }
}
