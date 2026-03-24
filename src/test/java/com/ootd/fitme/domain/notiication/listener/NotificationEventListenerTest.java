package com.ootd.fitme.domain.notiication.listener;

import com.ootd.fitme.domain.notiication.event.DirectMessageReceivedEvent;
import com.ootd.fitme.domain.notiication.service.NotificationService;
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

    @Test
    void 이벤트_받으면_서비스_호출된다() {

        UUID userId = UUID.randomUUID();

        DirectMessageReceivedEvent event =
                new DirectMessageReceivedEvent(userId, "sender", "msg");

        listener.DirectMessageEvent(event);

        verify(notificationService)
                .notifyDirectMessage(userId, "sender", "msg");
    }
}