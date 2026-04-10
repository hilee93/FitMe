package com.ootd.fitme.domain.user.repository;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.dto.request.UserSearchCondition;
import com.ootd.fitme.domain.user.dto.response.CursorSlice;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.SortDirection;
import com.ootd.fitme.domain.user.enums.UserSortBy;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
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

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private EntityManager em;

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

    @Test
    @DisplayName("findUsersByCondition - 이메일 오름차순 커서 페이지네이션")
    void findUsersByCondition_emailAsc_cursorPaging_success() {
        User u1 = userRepository.save(User.create("a@fitme.com", "password"));
        User u2 = userRepository.save(User.create("b@fitme.com", "password"));
        User u3 = userRepository.save(User.create("c@fitme.com", "password"));

        profileRepository.save(Profile.createDefault("a", u1));
        profileRepository.save(Profile.createDefault("b", u2));
        profileRepository.save(Profile.createDefault("c", u3));

        em.flush();
        em.clear();

        UserSearchCondition first = new UserSearchCondition(
                null, null, 2, UserSortBy.EMAIL, SortDirection.ASCENDING, null, null, null
        );

        CursorSlice<UserDto> firstSlice = userRepository.findUsersByCondition(first);

        assertThat(firstSlice.data()).hasSize(2);
        assertThat(firstSlice.hasNext()).isTrue();
        assertThat(firstSlice.totalCount()).isEqualTo(3L);
        assertThat(firstSlice.data().get(0).email()).isEqualTo("a@fitme.com");
        assertThat(firstSlice.data().get(1).email()).isEqualTo("b@fitme.com");

        em.flush();
        em.clear();

        UserSearchCondition second = new UserSearchCondition(
                firstSlice.nextCursor(),
                firstSlice.nextIdAfter(),
                2,
                UserSortBy.EMAIL,
                SortDirection.ASCENDING,
                null,
                null,
                null
        );

        CursorSlice<UserDto> secondSlice = userRepository.findUsersByCondition(second);

        assertThat(secondSlice.data()).hasSize(1);
        assertThat(secondSlice.hasNext()).isFalse();
        assertThat(secondSlice.data().get(0).email()).isEqualTo("c@fitme.com");
    }

}
