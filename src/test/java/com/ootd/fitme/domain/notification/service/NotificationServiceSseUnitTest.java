package com.ootd.fitme.domain.notification.service;


import com.ootd.fitme.domain.follow.repository.FollowRepository;
import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.enums.NotificationLevel;
import com.ootd.fitme.domain.notification.entity.NotificationFactory;
import com.ootd.fitme.domain.notification.repository.NotificationProfileRepository;
import com.ootd.fitme.domain.notification.repository.NotificationRepository;

import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceSseUnitTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationFactory notificationFactory;
    @Mock
    private NotificationSseService notificationSseService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private NotificationProfileRepository notificationProfileRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Nested
    @DisplayName("단건 알림 테스트")
    class SingleNotificationTest {

        @Test
        @DisplayName("팔로우 알림 저장 후 SSE 전송이 호출된다")
        void shouldSendSseAfterNotifyFollowed() {
            // given
            UUID followeeId = UUID.randomUUID();

            User user = mock(User.class);
            Notification notification = mock(Notification.class);
            Notification saved = mock(Notification.class);

            given(userRepository.findById(followeeId)).willReturn(Optional.of(user));
            given(notificationFactory.followed(user, "follower")).willReturn(notification);
            given(notificationRepository.save(notification)).willReturn(saved);

            given(saved.getId()).willReturn(UUID.randomUUID());
            given(saved.getCreatedAt()).willReturn(Instant.now());
            given(saved.getUser()).willReturn(user);
            given(user.getId()).willReturn(followeeId);
            given(saved.getTitle()).willReturn("새 팔로우");
            given(saved.getContent()).willReturn("follower님이 회원님을 팔로우했습니다.");
            given(saved.getLevel()).willReturn(NotificationLevel.INFO);

            // when
            Notification result = notificationService.notifyFollowed(followeeId, "follower");

            // then
            assertThat(result).isEqualTo(saved);

            verify(userRepository).findById(followeeId);
            verify(notificationFactory).followed(user, "follower");
            verify(notificationRepository).save(notification);
            verify(notificationSseService).send(eq(followeeId), any(NotificationDto.class));
        }
    }

    @Nested
    @DisplayName("다건 알림 테스트")
    class MultiNotificationTest {

        @Test
        @DisplayName("날씨 알림 저장 후 각 사용자에게 SSE 전송이 호출된다")
        void shouldSendSseAfterNotifyWeatherAlert() {
            // given
            String region1 = "서울";
            String region2 = "강남";
            String weatherAlert = "폭우";

            User user1 = mock(User.class);
            User user2 = mock(User.class);

            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            Notification notification1 = mock(Notification.class);
            Notification notification2 = mock(Notification.class);

            List<User> users = List.of(user1, user2);
            List<Notification> notifications = List.of(notification1, notification2);
            List<UUID> receiverIds = List.of(userId1, userId2);

            given(userRepository.findAllById(receiverIds)).willReturn(users);

            given(notificationFactory.weatherAlert(user1, region1, region2, weatherAlert))
                    .willReturn(notification1);
            given(notificationFactory.weatherAlert(user2, region1, region2, weatherAlert))
                    .willReturn(notification2);

            given(notificationRepository.saveAll(notifications)).willReturn(notifications);

            given(notification1.getId()).willReturn(UUID.randomUUID());
            given(notification1.getCreatedAt()).willReturn(Instant.now());
            given(notification1.getUser()).willReturn(user1);
            given(user1.getId()).willReturn(userId1);
            given(notification1.getTitle()).willReturn("날씨 알림");
            given(notification1.getContent()).willReturn("폭우");
            given(notification1.getLevel()).willReturn(NotificationLevel.WARN);

            given(notification2.getId()).willReturn(UUID.randomUUID());
            given(notification2.getCreatedAt()).willReturn(Instant.now());
            given(notification2.getUser()).willReturn(user2);
            given(user2.getId()).willReturn(userId2);
            given(notification2.getTitle()).willReturn("날씨 알림");
            given(notification2.getContent()).willReturn("폭우");
            given(notification2.getLevel()).willReturn(NotificationLevel.WARN);

            // when
            List<Notification> result = notificationService.notifyWeatherAlert(receiverIds, region1, region2, weatherAlert);

            // then
            assertThat(result).containsExactly(notification1, notification2);

            verify(userRepository).findAllById(receiverIds);
            verify(notificationFactory).weatherAlert(user1, region1, region2, weatherAlert);
            verify(notificationFactory).weatherAlert(user2, region1, region2, weatherAlert);
            verify(notificationRepository).saveAll(notifications);
            verify(notificationSseService, times(2))
                    .send(any(UUID.class), any(NotificationDto.class));
        }
    }

    @Nested
    @DisplayName("전체 전송 테스트")
    class BroadcastNotificationTest {

        @Test
        @DisplayName("속성 추가 알림 저장 후 sendAll 이 호출된다")
        void shouldSendAllAfterNotifyAttributeAdded() {
            // given
            String attributeName = "색상";

            User user1 = mock(User.class);
            User user2 = mock(User.class);

            UUID userId1 = UUID.randomUUID();

            Notification notification1 = mock(Notification.class);
            Notification notification2 = mock(Notification.class);

            List<User> users = List.of(user1, user2);
            List<Notification> notifications = List.of(notification1, notification2);

            given(userRepository.findAll()).willReturn(users);
            given(notificationFactory.attributeAdded(user1, attributeName)).willReturn(notification1);
            given(notificationFactory.attributeAdded(user2, attributeName)).willReturn(notification2);
            given(notificationRepository.saveAll(notifications)).willReturn(notifications);

            // sendAll은 saved.get(0)을 dto로 바꾸므로 첫 번째 notification만 세팅하면 됨
            given(notification1.getId()).willReturn(UUID.randomUUID());
            given(notification1.getCreatedAt()).willReturn(Instant.now());
            given(notification1.getUser()).willReturn(user1);
            given(user1.getId()).willReturn(userId1);
            given(notification1.getTitle()).willReturn("속성 추가");
            given(notification1.getContent()).willReturn("새 속성이 추가되었습니다.");
            given(notification1.getLevel()).willReturn(NotificationLevel.INFO);

            // when
            List<Notification> result = notificationService.notifyAttributeAdded(attributeName);

            // then
            assertThat(result).containsExactly(notification1, notification2);

            verify(userRepository).findAll();
            verify(notificationFactory).attributeAdded(user1, attributeName);
            verify(notificationFactory).attributeAdded(user2, attributeName);
            verify(notificationRepository).saveAll(notifications);
            verify(notificationSseService).sendAll(any(NotificationDto.class));
        }
    }
}