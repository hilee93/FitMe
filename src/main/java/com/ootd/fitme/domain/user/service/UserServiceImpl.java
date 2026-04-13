package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.exception.ProfileException;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.dto.request.*;
import com.ootd.fitme.domain.user.dto.response.*;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.enums.SortDirection;
import com.ootd.fitme.domain.user.enums.UserSortBy;
import com.ootd.fitme.domain.user.event.TemporaryPasswordMailRequestedEvent;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.mapper.UserMapper;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordMailSender;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordStore;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.jwt.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final Duration TEMP_PASSWORD_TTL = Duration.ofMinutes(3);
    private static final int DEFAULT_USER_LIST_LIMIT = 20;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final TokenBlacklistService tokenBlacklistService;
    private final TemporaryPasswordStore temporaryPasswordStore;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean matchesPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    @Transactional
    @Override
    public UserDto signUp(UserCreateRequest userCreateRequest) {
        if (userRepository.findByEmail(userCreateRequest.email()).isPresent()) {
            throw new UserException(ErrorCode.USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(userCreateRequest.password());
        User user = User.create(userCreateRequest.email(), encodedPassword);
        User saved = userRepository.save(user);

        Profile profile = Profile.create(
                userCreateRequest.name(),
                null, // longitude
                null, // latitude
                null, // x
                null, // y
                null, // region1
                null, // region2
                null, // region3
                null, // gender
                null, // birthDate
                null, // profileImageUrl
                saved
        );

        profileRepository.save(profile);

        return mapToUserDto(saved);
    }

    @Transactional
    @Override
    public UserDto updateRole(UUID userId, UserRoleUpdateRequest userRoleUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateRole(userRoleUpdateRequest.role());

        tokenBlacklistService.setRevokeAllBefore(userId, nowSeconds());

        return mapToUserDto(user);
    }

    @Transactional
    @Override
    public UserDto updateLock(UUID userId, UserLockUpdateRequest userLockUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateLocked(userLockUpdateRequest.locked());

        tokenBlacklistService.setRevokeAllBefore(userId, nowSeconds());

        return mapToUserDto(user);
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = userRepository.findByEmail(resetPasswordRequest.email())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String tempPassword = generateTemporaryPassword();
        String encodedTempPassword = passwordEncoder.encode(tempPassword);
        Instant expiresAt = Instant.now().plus(TEMP_PASSWORD_TTL);

        temporaryPasswordStore.save(
                user.getId(),
                encodedTempPassword,
                expiresAt
        );

        try {
            eventPublisher.publishEvent(new TemporaryPasswordMailRequestedEvent(
                    user.getId(),
                    user.getEmail(),
                    tempPassword,
                    TEMP_PASSWORD_TTL,
                    Instant.now()
            ));
        } catch (RuntimeException e) {
            temporaryPasswordStore.delete(user.getId());
            log.error("[TEMP_PASSWORD_MAIL][EVENT_PUBLISH_FAIL] userEmail={}", maskEmail(user.getEmail()), e);
            throw new UserException(ErrorCode.USER_TEMP_PASSWORD_MAIL_SEND_FAILED);
        }

        tokenBlacklistService.setRevokeAllBefore(user.getId(), nowSeconds());
        log.info("[TEMP PASSWORD MAIL][ENQUEUED] userEmail={}", maskEmail(user.getEmail()));
    }

    @Transactional
    @Override
    public void changePassword(UUID userId, ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String encodedPassword = passwordEncoder.encode(changePasswordRequest.password());
        user.updatePassword(encodedPassword);

        temporaryPasswordStore.delete(userId);

        tokenBlacklistService.setRevokeAllBefore(userId, nowSeconds());
    }

    @Transactional(readOnly = true)
    @Override
    public UserDtoCursorResponse getUsers(UserSearchCondition userSearchCondition) {
        String cursor = userSearchCondition.cursor();

        if (cursor != null && cursor.isBlank()) {
            cursor = null;
        }

        String emailLike = userSearchCondition.emailLike();
        if (emailLike != null) {
            emailLike = emailLike.trim();
            if (emailLike.isEmpty()) {
                emailLike = null;
            }
        }

        int limit = userSearchCondition.limit() == null ? DEFAULT_USER_LIST_LIMIT : userSearchCondition.limit();
        UserSortBy sortBy = userSearchCondition.sortBy() == null ? UserSortBy.CREATED_AT : userSearchCondition.sortBy();
        SortDirection sortDirection = userSearchCondition.sortDirection() == null
                ? SortDirection.DESCENDING
                : userSearchCondition.sortDirection();

        if ((cursor == null) != (userSearchCondition.idAfter() == null)) {
            throw new UserException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (sortBy == UserSortBy.CREATED_AT && cursor != null) {
            try {
                Instant.parse(cursor);
            } catch (Exception ex) {
                throw new UserException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        UserSearchCondition queryCondition = new UserSearchCondition(
                cursor,
                userSearchCondition.idAfter(),
                limit,
                sortBy,
                sortDirection,
                emailLike,
                userSearchCondition.roleEqual(),
                userSearchCondition.locked()
        );

        CursorSlice<UserDto> slice = userRepository.findUsersByCondition(queryCondition);

        return new UserDtoCursorResponse(
                slice.data(),
                slice.nextCursor(),
                slice.nextIdAfter(),
                slice.hasNext(),
                slice.totalCount(),
                sortBy.getValue(),
                sortDirection
        );
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);

        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            int idx = random.nextInt(TEMP_PASSWORD_CHARS.length());
            sb.append(TEMP_PASSWORD_CHARS.charAt(idx));
        }
        return sb.toString();
    }

    private Instant nowSeconds() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private UserDto mapToUserDto(User user) {
        String name = profileRepository.findByUserId(user.getId())
                .map(Profile::getName)
                .filter(profileName -> !profileName.isBlank())
                .orElseThrow(() -> new ProfileException(ErrorCode.PROFILE_NOT_FOUND));

        return userMapper.toDto(user, name);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }

        String local = email.substring(0, at);
        String domain = email.substring(at);

        if (local.length() == 1) {
            return local.charAt(0) + "***" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }
}
