package com.ootd.fitme.domain.notiication.service;

import com.ootd.fitme.domain.notiication.entity.Notification;
import com.ootd.fitme.domain.notiication.entity.NotificationFactory;
import com.ootd.fitme.domain.notiication.repository.NotificationRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("알림 생성")
    class CreateNotificationTest {

        @Test
        @DisplayName("DM 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenDirectMessageReceived() {
            UUID userId = UUID.randomUUID();
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationFactory.dm(user, "sender", "msg")).willReturn(notification);

            notificationService.notifyDirectMessage(userId, "sender", "msg");

            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("팔로우 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenFollowed() {
            UUID userId = UUID.randomUUID();
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationFactory.followed(user, "follower")).willReturn(notification);

            notificationService.notifyFollowed(userId, "follower");

            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("좋아요 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenFeedLiked() {
            UUID userId = UUID.randomUUID();
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationFactory.feedLiked(user, "liker")).willReturn(notification);

            notificationService.notifyFeedLiked(userId, "liker");

            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("댓글 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenFeedCommented() {
            UUID userId = UUID.randomUUID();
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationFactory.feedCommented(user, "commenter", "nice")).willReturn(notification);

            notificationService.notifyFeedCommented(userId, "commenter", "nice");

            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("속성 추가 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenAttributeAdded() {
            UUID userId = UUID.randomUUID();
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationFactory.attributeAdded(user, "color")).willReturn(notification);

            notificationService.notifyAttributeAdded(userId, "color");

            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("팔로우한 사용자 새 피드 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenFollowerNewFeed() {
            UUID userId = UUID.randomUUID();
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationFactory.followerNewFeed(user, "writer", "feed")).willReturn(notification);

            notificationService.notifyFollowerNewFeed(userId, "writer", "feed");

            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("날씨 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenWeatherAlert() {
            UUID userId = UUID.randomUUID();
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationFactory.weatherAlert(user, "비 온다")).willReturn(notification);

            notificationService.notifyWeatherAlert(userId, "비 온다");

            verify(notificationRepository).save(notification);
        }
    }
}