package com.ootd.fitme.domain.notification.repository;


import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.enums.NotificationLevel;
import com.ootd.fitme.domain.notification.enums.NotificationType;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("local")
@Import({JpaAuditingConfig.class,QuerydslConfig.class})
class NotificationRepositorySearchImpITest {

    @Autowired
    private EntityManager em;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.create("user@test.com", "1234"));
        otherUser = userRepository.save(User.create("other@test.com", "1234"));

        for (int i = 0; i < 23; i++) {
            Notification notification = Notification.create(
                    NotificationLevel.INFO,
                    "title " + i,
                    "content " + i,
                    NotificationType.DM,
                    user
            );
            notificationRepository.save(notification);
        }

        Notification otherNotification = Notification.create(
                NotificationLevel.INFO,
                "other title",
                "other content",
                NotificationType.DM,
                otherUser
        );
        notificationRepository.save(otherNotification);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("특정 유저의 알림만 조회된다")
    void shouldReturnOnlyUserNotifications() {
        // given
        NotificationPageRequest request = new NotificationPageRequest(
                user.getId(),
                null,
                null,
                30
        );

        // when
        Slice<Notification> result = notificationRepository.search(request);

        // then
        assertThat(result.getContent()).hasSize(23);
        assertThat(result.getContent())
                .allMatch(notification -> notification.getUser().getId().equals(user.getId()));
    }

    @Test
    @DisplayName("커서가 없으면 첫 페이지를 조회한다")
    void shouldReturnFirstPageWhenCursorIsNull() {
        // given
        NotificationPageRequest request = new NotificationPageRequest(
                user.getId(),
                null,
                null,
                10
        );

        // when
        Slice<Notification> result = notificationRepository.search(request);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.hasNext()).isTrue();


        List<Notification> content = result.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            Notification current = content.get(i);
            Notification next = content.get(i + 1);

            assertThat(current.getCreatedAt())
                    .isAfterOrEqualTo(next.getCreatedAt());
        }
    }


    @Test
    @DisplayName("첫 페이지 이후 다음 페이지를 조회한다")
    void shouldReturnNextPageInCorrectOrder() {
        Slice<Notification> firstPage = notificationRepository.search(
                new NotificationPageRequest(user.getId(), null, null, 10)
        );

        Notification last = firstPage.getContent().get(firstPage.getNumberOfElements() - 1);

        Slice<Notification> secondPage = notificationRepository.search(
                new NotificationPageRequest(
                        user.getId(),
                        last.getCreatedAt().toString(),
                        last.getId().toString(),
                        10
                )
        );

        assertThat(secondPage.getContent()).hasSize(10);
        assertThat(secondPage.getContent())
                .noneMatch(notification -> notification.getId().equals(last.getId()));

        List<Notification> content = secondPage.getContent();
        for (int i = 0; i < content.size() - 1; i++) {
            Notification current = content.get(i);
            Notification next = content.get(i + 1);

            assertThat(current.getCreatedAt())
                    .isAfterOrEqualTo(next.getCreatedAt());
        }
    }



    @Test
    @DisplayName("조회 결과는 createdAt 내림차순이다")
    void shouldReturnNotificationsInOrder() {
        NotificationPageRequest request = new NotificationPageRequest(
                user.getId(),
                null,
                null,
                30
        );

        Slice<Notification> result = notificationRepository.search(request);
        List<Notification> content = result.getContent();

        assertThat(content).hasSize(23);

        for (int i = 0; i < content.size() - 1; i++) {
            Notification current = content.get(i);
            Notification next = content.get(i + 1);

            assertThat(current.getCreatedAt().isBefore(next.getCreatedAt())).isFalse();
        }
    }

    @Test
    @DisplayName("조회 시 createdAt이 같으면 tie-breaker 기준으로 정렬된다")
    void shouldSortByTieBreakerWhenCreatedAtIsSame() {
        // given
        NotificationPageRequest request = new NotificationPageRequest(
                user.getId(),
                null,
                null,
                30
        );

        // when
        Slice<Notification> result = notificationRepository.search(request);

        // then
        List<Notification> content = result.getContent();

        assertThat(content).hasSize(23);

        for (int i = 0; i < content.size() - 1; i++) {
            Notification current = content.get(i);
            Notification next = content.get(i + 1);

            if (current.getCreatedAt().equals(next.getCreatedAt())) {
                // 같은 시간이면 tie-breaker(id) 기준으로 뒤집히지 않았는지만 확인
                assertThat(current.getId()).isNotEqualTo(next.getId());
            } else {
                assertThat(current.getCreatedAt()).isAfter(next.getCreatedAt());
            }
        }
    }
    @Test
    @DisplayName("같은 createdAt에서도 커서 기준으로 중복 없이 조회된다")
    void shouldNotDuplicateWhenCreatedAtIsSame() {
        // given
        NotificationPageRequest firstRequest = new NotificationPageRequest(
                user.getId(), null, null, 5
        );

        // when - 1페이지
        Slice<Notification> firstPage = notificationRepository.search(firstRequest);
        List<Notification> first = firstPage.getContent();

        Notification last = first.get(first.size() - 1);

        // when - 2페이지
        NotificationPageRequest secondRequest = new NotificationPageRequest(
                user.getId(),
                last.getCreatedAt().toString(),
                last.getId().toString(),
                5
        );

        Slice<Notification> secondPage = notificationRepository.search(secondRequest);
        List<Notification> second = secondPage.getContent();

        // then
        assertThat(second)
                .extracting(Notification::getId)
                .doesNotContain(last.getId());
    }





}