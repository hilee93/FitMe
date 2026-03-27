package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.user.dto.request.*;
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
import org.mockito.ArgumentCaptor;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private TemporaryPasswordStore temporaryPasswordStore;

    @InjectMocks
    private UserServiceImpl userService;

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
                false);
    }

    @Nested
    @DisplayName("signUp")
    class SignUpTest {
        @Test
        @DisplayName("성공 - 비밀번호 인코딩 후 저장하고 UserDto를 반환한다.")
        void signUp_success() {
            UserCreateRequest request = new UserCreateRequest("tester", email, rawPassword);
            User savedUser = mock(User.class);

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(userMapper.toDto(savedUser)).willReturn(userDto);

            UserDto result = userService.signUp(request);

            assertThat(result).isEqualTo(userDto);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo(email);
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(encodedPassword);
        }

        @Test
        @DisplayName("실패 - 중복 이메일이면 USER_ALREADY_EXISTS 예외")
        void signUp_duplicateEmail_fail() {
            UserCreateRequest request = new UserCreateRequest("tester", email, rawPassword);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(mock(User.class)));

            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(UserException.class)
                    .extracting(ex -> ((UserException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_ALREADY_EXISTS);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("signIn")
    class SignInTest {
        @Test
        @DisplayName("성공 - 일반 비밀번호로 로그인")
        void signIn_normalPassword_success() {
            User user = mock(User.class);

            given(user.getId()).willReturn(userId);
            given(user.getPassword()).willReturn(encodedPassword);
            given(user.getRole()).willReturn(Role.USER);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);
            given(jwtProvider.generateAccessToken(userId, "USER")).willReturn("access-token");
            given(jwtProvider.generateRefreshToken(userId, "USER")).willReturn("refresh-token");
            given(userMapper.toDto(user)).willReturn(userDto);

            SignInResult result = userService.signIn(new SignInRequest(email, rawPassword));

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

            assertThatThrownBy(() -> userService.signIn(new SignInRequest(email, rawPassword)))
                    .isInstanceOf(UserException.class)
                    .extracting(ex -> ((UserException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_ACCOUNT_LOCKED);
        }

        @Test
        @DisplayName("성공 - 임시 비밀번호(만료 전) 로그인")
        void signIn_temporaryPassword_success() {
            User user = mock(User.class);

            given(user.getId()).willReturn(userId);
            given(user.getPassword()).willReturn(encodedPassword);
            given(user.getRole()).willReturn(Role.USER);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);
            given(temporaryPasswordStore.findValidEncodedPassword(userId)).willReturn(Optional.of("encoded-temp"));
            given(passwordEncoder.matches(rawPassword, "encoded-temp")).willReturn(true);
            given(jwtProvider.generateAccessToken(userId, "USER")).willReturn("access-token");
            given(jwtProvider.generateRefreshToken(userId, "USER")).willReturn("refresh-token");
            given(userMapper.toDto(user)).willReturn(userDto);

            SignInResult result = userService.signIn(new SignInRequest(email, rawPassword));

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

            assertThatThrownBy(() -> userService.signIn(new SignInRequest(email, rawPassword)))
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
            given(userMapper.toDto(user)).willReturn(userDto);

            JwtDto result = userService.refresh("refresh-token");

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

            assertThatThrownBy(() -> userService.refresh("refresh-token"))
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

            assertThatThrownBy(() -> userService.refresh("refresh-token"))
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

            userService.signOut("access-token", "refresh-token");

            verify(tokenBlacklistService).blacklist("jti-access", exp);
            verify(tokenBlacklistService).blacklist("jti-refresh", exp);
        }
    }

    @Nested
    @DisplayName("관리자 계정관리")
    class AdminAccountTest {
        @Test
        @DisplayName("성공 - 권한 변경 시 role 반영 + revokeAllBefore 갱신")
        void updateRole_success() {
            User user = mock(User.class);
            UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(userDto);

            userService.updateRole(userId, request);

            verify(user).updateRole(Role.ADMIN);
            verify(tokenBlacklistService).setRevokeAllBefore(eq(userId), any(Instant.class));
        }

        @Test
        @DisplayName("성공 - 잠금 변경 시 locked 반영 + revokeAllBefore 갱신")
        void updateLock_success() {
            User user = mock(User.class);
            UserLockUpdateRequest request = new UserLockUpdateRequest(true);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(userDto);

            userService.updateLock(userId, request);

            verify(user).updateLocked(true);
            verify(tokenBlacklistService).setRevokeAllBefore(eq(userId), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("비밀번호 초기화")
    class PasswordResetTest {
        @Test
        @DisplayName("성공 - resetPassword 시 임시 비밀번호 저장(3분 만료) + revokeAllBefore")
        void resetPassword_success() {
            User user = mock(User.class);

            given(user.getId()).willReturn(userId);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.encode(anyString())).willReturn("encoded-temp");

            userService.resetPassword(new ResetPasswordRequest(email));

            ArgumentCaptor<Instant> expiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(temporaryPasswordStore).save(eq(userId), eq("encoded-temp"), expiresAtCaptor.capture());

            Instant expiresAt = expiresAtCaptor.getValue();
            Instant now = Instant.now();
            assertThat(expiresAt).isAfter(now.plusSeconds(150));
            assertThat(expiresAt).isBefore(now.plusSeconds(210));

            verify(tokenBlacklistService).setRevokeAllBefore(eq(userId), any(Instant.class));
        }

        @Test
        @DisplayName("성공 - changePassword 시 비밀번호 변경 + 임시 비밀번호 파기 + revokeAllBefore")
        void changePassword_success() {
            User user = mock(User.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.encode("new-password!")).willReturn("encoded-new");

            userService.changePassword(userId, new ChangePasswordRequest("new-password!"));

            verify(user).updatePassword("encoded-new");
            verify(temporaryPasswordStore).delete(userId);
            verify(tokenBlacklistService).setRevokeAllBefore(eq(userId), any(Instant.class));
        }
    }
}
