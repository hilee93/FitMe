package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.exception.auth.AuthException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.jwt.JwtProvider;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceIntegrationTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("signIn / refresh")
    class AuthFlowTest {
        @Test
        @DisplayName("성공 - signIn 시 토큰 발급 + revokeAllBefore 갱신")
        void signIn_success_updates_cutoff_and_issues_token() {
            User user = persistUser("signin-password1!", Role.USER, false);

            SignInResult result = authService.signIn(new SignInRequest(user.getEmail(), "signin-password1!"));

            assertThat(jwtProvider.validateToken(result.jwtDto().accessToken())).isTrue();
            assertThat(jwtProvider.isAccessToken(result.jwtDto().accessToken())).isTrue();
            assertThat(jwtProvider.validateToken(result.refreshToken())).isTrue();
            assertThat(jwtProvider.isRefreshToken(result.refreshToken())).isTrue();
            assertThat(tokenBlacklistService.getRevokeAllBefore(user.getId())).isNotNull();
        }

        @Test
        @DisplayName("성공 - refresh 시 새 accessToken 발급")
        void refresh_success_returns_new_access_token() {
            User user = persistUser("refresh-password1!", Role.USER, false);

            SignInResult signInResult = authService.signIn(new SignInRequest(user.getEmail(), "refresh-password1!"));

            JwtDto refreshed = authService.refresh(signInResult.refreshToken());

            assertThat(jwtProvider.validateToken(refreshed.accessToken())).isTrue();
            assertThat(jwtProvider.isAccessToken(refreshed.accessToken())).isTrue();
            assertThat(jwtProvider.getUserId(refreshed.accessToken())).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("실패 - cutoff 이후로 판정된 refresh 토큰은 거부")
        void refresh_fail_when_token_is_older_than_cutoff() {
            User user = persistUser("refresh-password2!", Role.USER, false);

            SignInResult signInResult = authService.signIn(new SignInRequest(user.getEmail(), "refresh-password2!"));

            String refreshToken = signInResult.refreshToken();
            Instant iat = jwtProvider.getIssuedAt(refreshToken);
            tokenBlacklistService.setRevokeAllBefore(user.getId(), iat.plusSeconds(1));

            assertThatThrownBy(() -> authService.refresh(refreshToken))
                    .isInstanceOf(AuthException.class)
                    .extracting(ex -> ((AuthException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
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

        em.flush();
        em.clear();

        return saved;
    }
}
