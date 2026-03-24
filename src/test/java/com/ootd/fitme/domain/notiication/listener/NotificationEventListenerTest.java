package com.ootd.fitme.domain.notiication.listener;

import com.ootd.fitme.domain.notiication.event.DirectMessageReceivedEvent;
import com.ootd.fitme.domain.notiication.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("이벤트를 받으면 알림 서비스가 호출된다")
    void shouldCallNotificationService_whenEventReceived() {

        UUID userId = UUID.randomUUID();

        DirectMessageReceivedEvent event =
                new DirectMessageReceivedEvent(userId, "sender", "msg");

        listener.directMessageEvent(event);

        verify(notificationService)
                .notifyDirectMessage(userId, "sender", "msg");
    }
}