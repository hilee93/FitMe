package com.ootd.fitme.domain.notification.entity;

import com.ootd.fitme.domain.notification.enums.AttributeAction;
import com.ootd.fitme.domain.notification.enums.NotificationLevel;
import com.ootd.fitme.domain.notification.enums.NotificationType;
import com.ootd.fitme.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("NotificationFactory 테스트")
class NotificationFactoryTest {

    private final NotificationFactory notificationFactory = new NotificationFactory();

    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
    }

    @Nested
    @DisplayName("알림 생성")
    class CreateNotification {


        @Test
        @DisplayName("DM 알림을 생성한다")
        void createDmNotification() {

            Notification notification = notificationFactory.dm(user, "제원", "안녕하세요");

            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getTitle()).isEqualTo("[제원]님이 메시지를 보냈습니다.");
            assertThat(notification.getContent()).isEqualTo("안녕하세요");
            assertThat(notification.getType()).isEqualTo(NotificationType.DM);
            assertThat(notification.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("팔로우 알림을 생성한다")
        void create_Followed_Notification() {

            Notification notification = notificationFactory.followed(user, "제원");

            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getTitle()).isEqualTo("[제원]님이 회원님을 팔로우했습니다.");
            assertThat(notification.getContent()).isEqualTo("팔로워 리스트를 확인하세요.");
            assertThat(notification.getType()).isEqualTo(NotificationType.FOLLOWED);
            assertThat(notification.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("날씨 알림을 생성한다")
        void create_Weather_AlertNotification() {

            Notification notification = notificationFactory.weatherAlert(user,"지역1","지역2", "내일 비가 올 예정입니다");

            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getTitle()).isEqualTo("[지역1 지역2]의 날씨가 [내일 비가 올 예정입니다]로 바뀌었습니다");
            assertThat(notification.getContent()).isEqualTo("날씨를 확인해 보세요");
            assertThat(notification.getType()).isEqualTo(NotificationType.WEATHER_ALERT);
            assertThat(notification.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("피드 좋아요 알림을 생성한다")
        void create_FeedLiked_Notification() {

            Notification notification = notificationFactory.feedLiked(user, "피드이름","제원");

            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getTitle()).isEqualTo("[제원]님이 회원님의 피드에 좋아요를 눌렀습니다.");
            assertThat(notification.getContent()).isEqualTo("피드: 피드이름");
            assertThat(notification.getType()).isEqualTo(NotificationType.FEED_LIKED);
            assertThat(notification.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("피드 댓글 알림을 생성한다")
        void create_FeedCommented_Notification() {

            Notification notification = notificationFactory.feedCommented(user, "제원","좋은 글이네요!");

            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getTitle()).isEqualTo("[제원]님이 댓글을 남겼습니다.");
            assertThat(notification.getContent()).isEqualTo("좋은 글이네요!");
            assertThat(notification.getType()).isEqualTo(NotificationType.FEED_COMMENTED);
            assertThat(notification.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("팔로우한 사용자의 새 피드 알림을 생성한다")
        void createFollowerNewFeedNotification() {

            Notification notification = notificationFactory.followerNewFeed(user, "제원", "봄 코디 추천");

            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getTitle()).isEqualTo("[제원]님이 새로운 피드를 등록했습니다.");
            assertThat(notification.getContent()).isEqualTo("봄 코디 추천");
            assertThat(notification.getType()).isEqualTo(NotificationType.FOLLOWER_NEW_FEED);
            assertThat(notification.getUser()).isEqualTo(user);
        }


        @Test
        @DisplayName("속성 추가 알림을 생성한다")
        void createAttributeAddedNotification() {

            Notification notification = notificationFactory.attributeAdded(user, "사이즈", AttributeAction.ADDED);

            assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
            assertThat(notification.getTitle()).isEqualTo("속성이 추가되었습니다.");
            assertThat(notification.getContent()).isEqualTo("[사이즈]속성을 확인해보세요");
            assertThat(notification.getType()).isEqualTo(NotificationType.ATTRIBUTE_ADDED);
            assertThat(notification.getUser()).isEqualTo(user);
        }

    }
}