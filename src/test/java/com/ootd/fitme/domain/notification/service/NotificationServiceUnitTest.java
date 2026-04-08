package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.follow.repository.FollowRepository;
import com.ootd.fitme.domain.notification.dto.request.NotificationDeleteRequest;
import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.dto.response.NotificationPageResponse;
import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.entity.NotificationFactory;
import com.ootd.fitme.domain.notification.enums.NotificationLevel;
import com.ootd.fitme.domain.notification.enums.NotificationType;
import com.ootd.fitme.domain.notification.exception.NotificationBadRequestException;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

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

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationSseService notificationSseService;

    @Mock
    private NotificationProfileRepository notificationProfileRepository;

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
            given(notificationFactory.feedLiked(user,"feedName", "liker")).willReturn(notification);

            notificationService.notifyFeedLiked(userId,"feedName", "liker");

            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("댓글 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenFeedCommented() {
            UUID userId = UUID.randomUUID();
            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationFactory.feedCommented(user,"feedName", "commenter", "nice")).willReturn(notification);

            notificationService.notifyFeedCommented(userId,"feedName", "commenter", "nice");

            verify(notificationRepository).save(notification);
        }

        @Test
        @DisplayName("속성 추가 알림이 정상적으로 생성된다")
        void shouldCreateNotification_whenAttributeAdded() {
            UUID userId1 = UUID.randomUUID();
            UUID notificationId1 = UUID.randomUUID();
            Instant now = Instant.now();

            User user1 = mock(User.class);
            User user2 = mock(User.class);

            Notification notification1 = mock(Notification.class);
            Notification notification2 = mock(Notification.class);

            List<User> users = List.of(user1, user2);
            List<Notification> notifications = List.of(notification1, notification2);


            given(user1.getId()).willReturn(userId1);

            given(notification1.getId()).willReturn(notificationId1);
            given(notification1.getCreatedAt()).willReturn(now);
            given(notification1.getUser()).willReturn(user1);
            given(notification1.getTitle()).willReturn("title1");
            given(notification1.getContent()).willReturn("content1");
            given(notification1.getLevel()).willReturn(NotificationLevel.INFO);


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
            verify(notificationSseService).sendAll(any(NotificationDto.class));
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
            given(follower.getId()).willReturn(followerId);
            given(notification.getUser()).willReturn(follower);
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

        @DisplayName("날씨 알림이 정상적으로 생성된다")
        @Test
        void shouldCreateNotification_whenWeatherAlert() {
            // given
            String region1 = "서울특별시";
            String region2 = "강남구";
            String weatherAlert = "강한 비";

            UUID userId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();

            User user = mock(User.class);
            Notification notification = mock(Notification.class);

            List<User> users = List.of(user);
            List<Notification> notifications = List.of(notification);
            List<UUID> receiverIds = List.of(userId);

            given(notificationRepository.saveAll(notifications))
                    .willReturn(notifications);

            // NotificationMapper.toDto(notification)
            given(user.getId()).willReturn(userId);
            given(notification.getId()).willReturn(notificationId);
            given(notification.getCreatedAt()).willReturn(Instant.now());
            given(notification.getUser()).willReturn(user);
            given(notification.getTitle()).willReturn("날씨 알림");
            given(notification.getContent()).willReturn(weatherAlert);
            given(notification.getLevel()).willReturn(NotificationLevel.INFO);
            given(userRepository.findAllById(receiverIds)).willReturn(users);
            given(notificationFactory.weatherAlert(user,region1,region2, weatherAlert)).willReturn(notification);

            // when
            List<Notification> result = notificationService.notifyWeatherAlert(receiverIds,region1, region2, weatherAlert);

            // then
            assertThat(result).containsExactly(notification);

            verify(userRepository).findAllById(receiverIds);
            verify(notificationFactory).weatherAlert(user,region1,region2, weatherAlert);
            verify(notificationRepository).saveAll(notifications);
            verify(notificationSseService).send(eq(userId), any(NotificationDto.class));
        }
    }
    @Nested
    @DisplayName("알림 목록조회")
    class SearchNotificationTest {

        @Test
        @DisplayName("다음 페이지가 있으면 마지막 알림 기준으로 nextCursor와 nextIdAfter를 반환한다")
        void shouldReturnNextCursorAndNextIdAfterWhenHasNext() {
            UUID userId = UUID.randomUUID();
            NotificationPageRequest request = new NotificationPageRequest(userId, null, null, 10);

            User user = User.create("user@test.com", "1234");

            Notification first = Notification.create(
                    NotificationLevel.INFO,
                    "title 1",
                    "content 1",
                    NotificationType.DM,
                    user
            );

            Notification last = Notification.create(
                    NotificationLevel.INFO,
                    "title 2",
                    "content 2",
                    NotificationType.DM,
                    user
            );

            Instant firstCreatedAt = Instant.parse("2026-03-26T10:00:00Z");
            Instant lastCreatedAt = Instant.parse("2026-03-26T09:00:00Z");
            UUID lastId = UUID.randomUUID();

            ReflectionTestUtils.setField(first, "createdAt", firstCreatedAt);
            ReflectionTestUtils.setField(last, "createdAt", lastCreatedAt);
            ReflectionTestUtils.setField(last, "id", lastId);

            Slice<Notification> slice = new SliceImpl<>(List.of(first, last), PageRequest.of(0, 10), true);

            given(notificationRepository.search(request)).willReturn(slice);
            given(notificationRepository.countByUserId(userId)).willReturn(23L);

            NotificationPageResponse result = notificationService.getNotifications(request);

            assertThat(result.data()).hasSize(2);
            assertThat(result.nextCursor()).isEqualTo(lastCreatedAt.toString());
            assertThat(result.nextIdAfter()).isEqualTo(lastId.toString());
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("다음 페이지가 없으면 nextCursor와 nextIdAfter는 null이다")
        void shouldReturnNullCursorWhenHasNoNext() {
            UUID userId = UUID.randomUUID();
            NotificationPageRequest request = new NotificationPageRequest(userId, null, null, 10);

            User user = User.create("user@test.com", "1234");

            Notification notification = Notification.create(
                    NotificationLevel.INFO,
                    "title 1",
                    "content 1",
                    NotificationType.DM,
                    user
            );

            Instant createdAt = Instant.parse("2026-03-26T10:00:00Z");
            ReflectionTestUtils.setField(notification, "createdAt", createdAt);

            Slice<Notification> slice = new SliceImpl<>(List.of(notification), PageRequest.of(0, 10), false);

            given(notificationRepository.search(request)).willReturn(slice);
            given(notificationRepository.countByUserId(userId)).willReturn(1L);

            NotificationPageResponse result = notificationService.getNotifications(request);

            assertThat(result.data()).hasSize(1);
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextIdAfter()).isNull();
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("알림 삭제")
    class DeleteNotificationTest{

        @Test
        @DisplayName("성공 - 본인 알림이면 삭제한다")
        void delete_success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();
            NotificationDeleteRequest request = new NotificationDeleteRequest(userId, notificationId);

            given(notificationRepository.existsByIdAndUserId(notificationId, userId))
                    .willReturn(true);

            // when
            notificationService.delete(request);

            // then
            then(notificationRepository).should().existsByIdAndUserId(notificationId, userId);
            then(notificationRepository).should().deleteById(notificationId);
        }

        @Test
        @DisplayName("실패 - 본인 알림이 아니거나 알림이 없으면 예외를 던진다")
        void delete_fail_when_notification_not_owned_or_not_found() {
            // given
            UUID userId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();
            NotificationDeleteRequest request = new NotificationDeleteRequest(userId, notificationId);

            given(notificationRepository.existsByIdAndUserId(notificationId, userId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> notificationService.delete(request))
                    .isInstanceOf(NotificationBadRequestException.class);

            then(notificationRepository).should().existsByIdAndUserId(notificationId, userId);
            then(notificationRepository).should(never()).deleteById(any(UUID.class));
        }


    }
}

