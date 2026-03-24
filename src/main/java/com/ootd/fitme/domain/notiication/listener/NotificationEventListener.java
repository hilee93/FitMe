package com.ootd.fitme.domain.notiication.listener;


import com.ootd.fitme.domain.notiication.event.DirectMessageReceivedEvent;
import com.ootd.fitme.domain.notiication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener
    public void DirectMessageEvent(DirectMessageReceivedEvent event) {

        notificationService.notifyDirectMessage(
                event.receiverId(),
                event.senderName(),
                event.message()
        );
    }

}
