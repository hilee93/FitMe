package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.follow.repository.FollowRepository;
import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.entity.NotificationFactory;
import com.ootd.fitme.domain.notification.repository.NotificationRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationFactory notificationFactory;

    @Mock
    private FollowRepository followRepository;

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
            User user1 = mock(User.class);
            User user2 = mock(User.class);

            Notification notification1 = mock(Notification.class);
            Notification notification2 = mock(Notification.class);

            List<User> users = List.of(user1, user2);
            List<Notification> notifications = List.of(notification1, notification2);

            given(userRepository.findAll()).willReturn(users);
            given(notificationFactory.attributeAdded(user1, "color")).willReturn(notification1);
            given(notificationFactory.attributeAdded(user2, "color")).willReturn(notification2);
            given(notificationRepository.saveAll(notifications)).willReturn(notifications);

            List<Notification> result = notificationService.notifyAttributeAdded("color");

            assertThat(result).isEqualTo(notifications);

            verify(userRepository).findAll();
            verify(notificationFactory).attributeAdded(user1, "color");
            verify(notificationFactory).attributeAdded(user2, "color");
            verify(notificationRepository).saveAll(notifications);
        }

        @Test
        @DisplayName("팔로우한 사용자 새 피드 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenFollowerNewFeed() {
            UUID followeeId = UUID.randomUUID();
            UUID followerId = UUID.randomUUID();

            User follower = mock(User.class);
            Notification notification = mock(Notification.class);

            List<UUID> followerIds = List.of(followerId);
            List<User> followers = List.of(follower);
            List<Notification> notifications = List.of(notification);

            given(followRepository.findFollowerIdsByFolloweeId(followeeId)).willReturn(followerIds);
            given(userRepository.findAllById(followerIds)).willReturn(followers);
            given(notificationFactory.followerNewFeed(follower, "writer", "feed")).willReturn(notification);
            given(notificationRepository.saveAll(notifications)).willReturn(notifications);

            List<Notification> result = notificationService.notifyFollowerNewFeed(followeeId, "writer", "feed");

            assertThat(result).containsExactly(notification);

            verify(followRepository).findFollowerIdsByFolloweeId(followeeId);
            verify(userRepository).findAllById(followerIds);
            verify(notificationFactory).followerNewFeed(follower, "writer", "feed");
            verify(notificationRepository).saveAll(notifications);
        }

        @Test
        @DisplayName("날씨 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenWeatherAlert() {
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            List<User> users = List.of(user);
            List<Notification> notifications = List.of(notification);

            given(userRepository.findAll()).willReturn(users);
            given(notificationFactory.weatherAlert(user, "비 온다")).willReturn(notification);
            given(notificationRepository.saveAll(notifications)).willReturn(notifications);

            List<Notification> result = notificationService.notifyWeatherAlert("비 온다");

            assertThat(result).containsExactly(notification);

            verify(userRepository).findAll();
            verify(notificationFactory).weatherAlert(user, "비 온다");
            verify(notificationRepository).saveAll(notifications);
        }
    }
}