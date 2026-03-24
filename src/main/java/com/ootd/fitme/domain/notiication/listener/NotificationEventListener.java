package com.ootd.fitme.domain.notiication.listener;


import com.ootd.fitme.domain.notiication.event.DirectMessageReceivedEvent;
import com.ootd.fitme.domain.notiication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async("eventTaskExecutor")
    @TransactionalEventListener
    public void directMessageEvent(DirectMessageReceivedEvent event) {

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
}
