package com.ootd.fitme.domain.userweathernotification.repository;

import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.userweathernotification.entity.UserWeatherNotification;
import com.ootd.fitme.domain.userweathernotification.enums.NoticeType;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class UserWeatherNotificationRepositoryTest {
    @Autowired
    private UserWeatherNotificationRepository userWeatherNotificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("findByUserIdAndNoticeType - 존재하면 해당 marker를 반환")
    void findByUserIdAndNoticeType_found() {
        User user = saveUser();
        Instant sentAt = Instant.parse("2026-04-08T01:00:00Z");

        userWeatherNotificationRepository.save(
                UserWeatherNotification.create(NoticeType.COLD_HEAT, sentAt, user.getId())
        );
        em.flush();
        em.clear();

        Optional<UserWeatherNotification> result =
                userWeatherNotificationRepository.findByUserIdAndNoticeType(user.getId(), NoticeType.COLD_HEAT);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(user.getId());
        assertThat(result.get().getNoticeType()).isEqualTo(NoticeType.COLD_HEAT);
        assertThat(result.get().getSentAt()).isEqualTo(sentAt);
    }

    @Test
    @DisplayName("unique(user_id, notice_type) - 동일 user/type 중복 저장은 실패")
    void uniqueConstraint_userAndNoticeType() {
        User user = saveUser();

        userWeatherNotificationRepository.save(
                UserWeatherNotification.create(NoticeType.TEMPERATURE_SWING, Instant.now(), user.getId())
        );
        em.flush();

        assertThatThrownBy(() -> {
            userWeatherNotificationRepository.save(
                    UserWeatherNotification.create(NoticeType.TEMPERATURE_SWING, Instant.now().plusSeconds(60), user.getId())
            );
            em.flush();
        }).isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
    }

    @Test
    @DisplayName("동일 user라도 noticeType이 다르면 저장 가능")
    void save_whenDifferentNoticeType_thenSuccess() {
        User user = saveUser();

        userWeatherNotificationRepository.save(
                UserWeatherNotification.create(NoticeType.PRECIPITATION_START, Instant.now(), user.getId())
        );

        userWeatherNotificationRepository.save(
                UserWeatherNotification.create(NoticeType.COLD_HEAT, Instant.now().plusSeconds(10), user.getId())
        );
        em.flush();
        em.clear();

        assertThat(
                userWeatherNotificationRepository.findByUserIdAndNoticeType(user.getId(), NoticeType.PRECIPITATION_START)
        ).isPresent();

        assertThat(
                userWeatherNotificationRepository.findByUserIdAndNoticeType(user.getId(), NoticeType.COLD_HEAT)
        ).isPresent();
    }

    private User saveUser() {
        String email = UUID.randomUUID() + "@fitme.com";
        return userRepository.save(User.create(email, "encoded-password"));
    }
}
