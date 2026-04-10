package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.dto.request.ChangePasswordRequest;
import com.ootd.fitme.domain.user.dto.request.ResetPasswordRequest;
import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.security.jwt.TokenBlacklistService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private TemporaryPasswordStore temporaryPasswordStore;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("signUp")
    class SignUpTest {
        @Test
        @DisplayName("성공 - 회원가입 시 DB 저장 + 비밀번호 인코딩")
        void signUp_success_persists_encoded_password() {
            String email = UUID.randomUUID() + "@fitme.com";
            String rawPassword = "sign-up-password1!";

            UserCreateRequest request = new UserCreateRequest("tester", email, rawPassword);

            UserDto result = userService.signUp(request);

            em.flush();
            em.clear();

            User saved = userRepository.findById(result.id()).orElseThrow();
            assertThat(saved.getEmail()).isEqualTo(email);
            assertThat(saved.getPassword()).isNotEqualTo(rawPassword);
            assertThat(passwordEncoder.matches(rawPassword, saved.getPassword())).isTrue();
            assertThat(result.name()).isEqualTo("tester");
        }
    }

    @Nested
    @DisplayName("changePassword / resetPassword")
    class PasswordFlowTest {
        @Test
        @DisplayName("성공 - changePassword 시 DB 반영 + 임시비밀번호 삭제 + cutoff 갱신")
        void changePassword_success_updates_db_and_deletes_temp_password() {
            User user = persistUser("old-password1!", Role.USER, false);

            temporaryPasswordStore.save(
                    user.getId(),
                    passwordEncoder.encode("temp-password1!"),
                    Instant.now().plusSeconds(180)
            );

            userService.changePassword(user.getId(), new ChangePasswordRequest("new-password1!"));

            em.flush();
            em.clear();

            User reloaded = userRepository.findById(user.getId()).orElseThrow();
            assertThat(passwordEncoder.matches("new-password1!", reloaded.getPassword())).isTrue();
            assertThat(passwordEncoder.matches("old-password1!", reloaded.getPassword())).isFalse();
            assertThat(temporaryPasswordStore.findValidEncodedPassword(user.getId())).isEmpty();
            assertThat(tokenBlacklistService.getRevokeAllBefore(user.getId())).isNotNull();
        }

        @Test
        @DisplayName("성공 - resetPassword 시 임시비밀번호 저장 + cutoff 갱신")
        void resetPassword_success_stores_temp_password_and_updates_cutoff() {
            User user = persistUser("old-password2!", Role.USER, false);

            userService.resetPassword(new ResetPasswordRequest(user.getEmail()));

            Optional<String> encodedTemp = temporaryPasswordStore.findValidEncodedPassword(user.getId());

            assertThat(encodedTemp).isPresent();
            assertThat(encodedTemp.get()).isNotBlank();
            assertThat(tokenBlacklistService.getRevokeAllBefore(user.getId())).isNotNull();
        }
    }

    private User persistUser(String rawPassword, Role role, boolean locked) {
        String email = UUID.randomUUID() + "@fitme.com";
        User user = User.create(email, passwordEncoder.encode(rawPassword));
        user.updateRole(role);
        user.updateLocked(locked);
        User saved = userRepository.save(user);

        String name = email.substring(0, email.indexOf('@'));
        profileRepository.save(Profile.createDefault(name, saved));
        return saved;
    }
}
