package com.ootd.fitme.domain.notiication.service;

import com.ootd.fitme.domain.notiication.entity.Notification;
import com.ootd.fitme.domain.notiication.entity.NotificationFactory;
import com.ootd.fitme.domain.notiication.repository.NotificationRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationFactory notificationFactory;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void DM_알림_생성된다() {

        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Notification notification = mock(Notification.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(notificationFactory.dm(user, "sender", "msg")).willReturn(notification);

        notificationService.notifyDirectMessage(userId, "sender", "msg");

        verify(notificationRepository).save(notification);
    }
}