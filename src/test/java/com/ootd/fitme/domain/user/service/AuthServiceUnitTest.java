package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.exception.auth.AuthException;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.mapper.UserMapper;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.jwt.JwtProvider;
import com.ootd.fitme.global.security.jwt.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private TemporaryPasswordStore temporaryPasswordStore;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private UUID userId;
    private String email;
    private String rawPassword;
    private String encodedPassword;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        email = "test@fitme.com";
        rawPassword = "password1!";
        encodedPassword = "encoded-password";
        userDto = new UserDto(
                userId,
                Instant.now(),
                email,
                "test",
                Role.USER,
                false
        );
    }

    @Nested
    @DisplayName("signIn")
    class SignInTest {
        @Test
        @DisplayName("성공 - 일반 비밀번호로 로그인")
        void signIn_normalPassword_success() {
            User user = mock(User.class);
            Profile profile = mockProfile("tester");

            given(user.getId()).willReturn(userId);
            given(user.getPassword()).willReturn(encodedPassword);
            given(user.getRole()).willReturn(Role.USER);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);
            given(jwtProvider.generateAccessToken(userId, "USER")).willReturn("access-token");
            given(jwtProvider.generateRefreshToken(userId, "USER")).willReturn("refresh-token");
            given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
            given(userMapper.toDto(any(User.class), any(String.class))).willReturn(userDto);

            SignInResult result = authService.signIn(new SignInRequest(email, rawPassword));

            assertThat(result.jwtDto().accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            verify(tokenBlacklistService).setRevokeAllBefore(eq(userId), any(Instant.class));
        }

        @Test
        @DisplayName("실패 - 잠긴 계정은 USER_ACCOUNT_LOCKED 예외")
        void signIn_lockedAccount_fail() {
            User user = mock(User.class);

            given(user.isLocked()).willReturn(true);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.signIn(new SignInRequest(email, rawPassword)))
                    .isInstanceOf(UserException.class)
                    .extracting(ex -> ((UserException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_ACCOUNT_LOCKED);
        }

        @Test
        @DisplayName("성공 - 임시 비밀번호(만료 전) 로그인")
        void signIn_temporaryPassword_success() {
            User user = mock(User.class);
            Profile profile = mockProfile("tester");

            given(user.getId()).willReturn(userId);
            given(user.getPassword()).willReturn(encodedPassword);
            given(user.getRole()).willReturn(Role.USER);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);
            given(temporaryPasswordStore.findValidEncodedPassword(userId)).willReturn(Optional.of("encoded-temp"));
            given(passwordEncoder.matches(rawPassword, "encoded-temp")).willReturn(true);
            given(jwtProvider.generateAccessToken(userId, "USER")).willReturn("access-token");
            given(jwtProvider.generateRefreshToken(userId, "USER")).willReturn("refresh-token");
            given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
            given(userMapper.toDto(any(User.class), any(String.class))).willReturn(userDto);

            SignInResult result = authService.signIn(new SignInRequest(email, rawPassword));

            assertThat(result.jwtDto().accessToken()).isEqualTo("access-token");
            verify(tokenBlacklistService).setRevokeAllBefore(eq(userId), any(Instant.class));
        }

        @Test
        @DisplayName("실패 - 임시 비밀번호 만료/불일치 시 AUTH_INVALID_CREDENTIALS")
        void signIn_temporaryPasswordInvalid_fail() {
            User user = mock(User.class);

            given(user.getId()).willReturn(userId);
            given(user.getPassword()).willReturn(encodedPassword);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);
            given(temporaryPasswordStore.findValidEncodedPassword(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.signIn(new SignInRequest(email, rawPassword)))
                    .isInstanceOf(AuthException.class)
                    .extracting(ex -> ((AuthException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    @Nested
    @DisplayName("refresh")
    class RefreshTest {
        @Test
        @DisplayName("성공 - 유효한 refresh token이면 새 access token 발급")
        void refresh_success() {
            User user = mock(User.class);
            Instant iat = Instant.now();
            Profile profile = mockProfile("tester");

            given(user.getRole()).willReturn(Role.USER);
            given(jwtProvider.validateToken("refresh-token")).willReturn(true);
            given(jwtProvider.isRefreshToken("refresh-token")).willReturn(true);
            given(jwtProvider.getUserId("refresh-token")).willReturn(userId);
            given(jwtProvider.getTokenId("refresh-token")).willReturn("jti-1");
            given(jwtProvider.getIssuedAt("refresh-token")).willReturn(iat);
            given(tokenBlacklistService.isBlacklisted("jti-1")).willReturn(false);
            given(tokenBlacklistService.getRevokeAllBefore(userId)).willReturn(iat.minusSeconds(1));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(jwtProvider.generateAccessToken(userId, "USER")).willReturn("new-access-token");
            given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
            given(userMapper.toDto(any(User.class), any(String.class))).willReturn(userDto);

            JwtDto result = authService.refresh("refresh-token");

            assertThat(result.accessToken()).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("실패 - 블랙리스트 토큰이면 AUTH_INVALID_TOKEN")
        void refresh_blacklisted_fail() {
            Instant iat = Instant.now();

            given(jwtProvider.validateToken("refresh-token")).willReturn(true);
            given(jwtProvider.isRefreshToken("refresh-token")).willReturn(true);
            given(jwtProvider.getUserId("refresh-token")).willReturn(userId);
            given(jwtProvider.getTokenId("refresh-token")).willReturn("jti-1");
            given(jwtProvider.getIssuedAt("refresh-token")).willReturn(iat);
            given(tokenBlacklistService.isBlacklisted("jti-1")).willReturn(true);

            assertThatThrownBy(() -> authService.refresh("refresh-token"))
                    .isInstanceOf(AuthException.class)
                    .extracting(ex -> ((AuthException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
        }

        @Test
        @DisplayName("실패 - cutoff 이전 발급 토큰이면 AUTH_INVALID_TOKEN")
        void refresh_cutoff_fail() {
            Instant iat = Instant.now();

            given(jwtProvider.validateToken("refresh-token")).willReturn(true);
            given(jwtProvider.isRefreshToken("refresh-token")).willReturn(true);
            given(jwtProvider.getUserId("refresh-token")).willReturn(userId);
            given(jwtProvider.getTokenId("refresh-token")).willReturn("jti-1");
            given(jwtProvider.getIssuedAt("refresh-token")).willReturn(iat);
            given(tokenBlacklistService.isBlacklisted("jti-1")).willReturn(false);
            given(tokenBlacklistService.getRevokeAllBefore(userId)).willReturn(iat.plusSeconds(1));

            assertThatThrownBy(() -> authService.refresh("refresh-token"))
                    .isInstanceOf(AuthException.class)
                    .extracting(ex -> ((AuthException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    @Nested
    @DisplayName("signOut")
    class SignOutTest {
        @Test
        @DisplayName("성공 - access/refresh 토큰을 블랙리스트에 등록")
        void signOut_success() {
            Instant exp = Instant.now().plusSeconds(60);

            given(jwtProvider.validateToken("access-token")).willReturn(true);
            given(jwtProvider.validateToken("refresh-token")).willReturn(true);
            given(jwtProvider.getTokenId("access-token")).willReturn("jti-access");
            given(jwtProvider.getTokenId("refresh-token")).willReturn("jti-refresh");
            given(jwtProvider.getExpiration("access-token")).willReturn(exp);
            given(jwtProvider.getExpiration("refresh-token")).willReturn(exp);

            authService.signOut("access-token", "refresh-token");

            verify(tokenBlacklistService).blacklist("jti-access", exp);
            verify(tokenBlacklistService).blacklist("jti-refresh", exp);
        }
    }

    private Profile mockProfile(String name) {
        Profile profile = mock(Profile.class);
        given(profile.getName()).willReturn(name);
        return profile;
    }
}
