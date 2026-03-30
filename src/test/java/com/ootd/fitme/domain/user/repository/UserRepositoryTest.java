package com.ootd.fitme.domain.user.repository;

import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail - 존재하는 이메일이면 User를 반환한다.")
    void findByEmail_exists_returnUser() {
        // given
        String email = "exists@fitme.com";
        userRepository.save(User.create(email, "encoded-password"));

        // when
        Optional<User> result = userRepository.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("findByEmail - 존재하지 않는 이메일이면 empty를 반환한다.")
    void findByEmail_notExists_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("not-found@fitme.com");

        assertThat(result).isEmpty();
    }
}
