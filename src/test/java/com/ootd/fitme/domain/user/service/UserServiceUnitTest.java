package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.dto.request.*;
import com.ootd.fitme.domain.user.dto.response.*;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.Role;
import com.ootd.fitme.domain.user.enums.SortDirection;
import com.ootd.fitme.domain.user.enums.UserSortBy;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.mapper.UserMapper;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailSender;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordStore;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.jwt.TokenBlacklistService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private TemporaryPasswordStore temporaryPasswordStore;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private TemporaryPasswordMailSender temporaryPasswordMailSender;

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
            Profile profile = mockProfile("tester");

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
            given(userMapper.toDto(any(User.class), anyString())).willReturn(userDto);

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

        @Test
        @DisplayName("성공 - 회원가입 시 user/profile 저장 후 UserDto 반환")
        void signUp_success_withProfile() {
            UserCreateRequest request = new UserCreateRequest("tester", email, rawPassword);
            User savedUser = mock(User.class);
            Profile profile = mockProfile("tester");

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(profileRepository.save(any(Profile.class))).willAnswer(inv -> inv.getArgument(0));
            given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
            given(userMapper.toDto(any(User.class), anyString())).willReturn(userDto);

            UserDto result = userService.signUp(request);

            assertThat(result).isEqualTo(userDto);
            verify(profileRepository).save(any(Profile.class));
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
            Profile profile = mockProfile("tester");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
            given(userMapper.toDto(any(User.class), anyString())).willReturn(userDto);

            userService.updateRole(userId, request);

            verify(user).updateRole(Role.ADMIN);
            verify(tokenBlacklistService).setRevokeAllBefore(eq(userId), any(Instant.class));
        }

        @Test
        @DisplayName("성공 - 잠금 변경 시 locked 반영 + revokeAllBefore 갱신")
        void updateLock_success() {
            User user = mock(User.class);
            UserLockUpdateRequest request = new UserLockUpdateRequest(true);
            Profile profile = mockProfile("tester");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(profileRepository.findByUserId(any())).willReturn(Optional.of(profile));
            given(userMapper.toDto(any(User.class), anyString())).willReturn(userDto);

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
            given(user.getEmail()).willReturn(email);

            userService.resetPassword(new ResetPasswordRequest(email));

            ArgumentCaptor<Instant> expiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(temporaryPasswordStore).save(eq(userId), eq("encoded-temp"), expiresAtCaptor.capture());

            Instant expiresAt = expiresAtCaptor.getValue();
            Instant now = Instant.now();
            assertThat(expiresAt).isAfter(now.plusSeconds(150));
            assertThat(expiresAt).isBefore(now.plusSeconds(210));

            verify(tokenBlacklistService).setRevokeAllBefore(eq(userId), any(Instant.class));
            verify(temporaryPasswordMailSender)
                    .sendTemporaryPassword(eq(email), anyString(), eq(Duration.ofMinutes(3)));
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

        @Test
        @DisplayName("실패 - 메일 발송 실패 시 임시 비밀번호 롤백 + USER_TEMP_PASSWORD_MAIL_SEND_FAILED")
        void resetPassword_mailSendFail_rollBackTempPassword() {
            User user = mock(User.class);

            given(user.getId()).willReturn(userId);
            given(user.getEmail()).willReturn(email);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.encode(anyString())).willReturn("encoded-temp");
            willThrow(new IllegalStateException("smtp fail"))
                    .given(temporaryPasswordMailSender)
                    .sendTemporaryPassword(eq(email), anyString(), eq(Duration.ofMinutes(3)));

            assertThatThrownBy(() -> userService.resetPassword(new ResetPasswordRequest(email)))
                    .isInstanceOf(UserException.class)
                    .extracting(ex -> ((UserException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_TEMP_PASSWORD_MAIL_SEND_FAILED);

            verify(temporaryPasswordStore).delete(userId);
            verify(tokenBlacklistService, never()).setRevokeAllBefore(eq(userId), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("getUsers")
    class GetUsers {
        @Test
        @DisplayName("성공 - 기본값/정규화 적용 후 커서 응답 반환")
        void getUsers_success_withNormalization() {
            CursorSlice<UserDto> slice = new CursorSlice<>(
                    List.of(userDto),
                    null,
                    null,
                    false,
                    1L
            );

            given(userRepository.findUsersByCondition(any(UserSearchCondition.class))).willReturn(slice);

            UserSearchCondition request = new UserSearchCondition(
                    "   ",
                    null,
                    null,
                    null,
                    null,
                    "   test@fitme.com",
                    null,
                    null
            );

            UserDtoCursorResponse result = userService.getUsers(request);

            ArgumentCaptor<UserSearchCondition> captor = ArgumentCaptor.forClass(UserSearchCondition.class);
            verify(userRepository).findUsersByCondition(captor.capture());

            UserSearchCondition applied = captor.getValue();
            assertThat(applied.cursor()).isNull();
            assertThat(applied.limit()).isEqualTo(20);
            assertThat(applied.sortBy()).isEqualTo(UserSortBy.CREATED_AT);
            assertThat(applied.sortDirection()).isEqualTo(SortDirection.DESCENDING);
            assertThat(applied.emailLike()).isEqualTo("test@fitme.com");

            assertThat(result.data()).hasSize(1);
            assertThat(result.totalCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 - cursor/idAfter 짝이 맞지 않으면 INVALID_INPUT_VALUE")
        void getUsers_fail_invalidCursorPair() {
            UserSearchCondition bad = new UserSearchCondition(
                    null,
                    UUID.randomUUID(),
                    20,
                    UserSortBy.CREATED_AT,
                    SortDirection.DESCENDING,
                    null,
                    null,
                    null
            );

            assertThatThrownBy(() -> userService.getUsers(bad))
                    .isInstanceOf(UserException.class)
                    .extracting(ex -> ((UserException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Profile mockProfile(String name) {
        Profile profile = mock(Profile.class);
        given(profile.getName()).willReturn(name);
        return profile;
    }
}
