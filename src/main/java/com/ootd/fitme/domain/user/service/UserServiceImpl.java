package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.exception.ProfileException;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.dto.request.*;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.auth.AuthException;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.mapper.UserMapper;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.jwt.JwtProvider;
import com.ootd.fitme.global.security.jwt.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

// TODO: 인증 관련 로직(signIn/토큰 발급)은 AuthService로 분리 검토
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final Duration TEMP_PASSWORD_TTL = Duration.ofMinutes(3);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final TemporaryPasswordStore temporaryPasswordStore;
    private final ProfileRepository profileRepository;

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

    @Transactional(readOnly = true)
    @Override
    public User validateSignIn(SignInRequest signInRequest) {
        User user = userRepository.findByEmail(signInRequest.username())
                .orElseThrow(() -> new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (user.isLocked()) {
            throw new UserException(ErrorCode.USER_ACCOUNT_LOCKED);
        }

        boolean normalPasswordMatched = passwordEncoder.matches(signInRequest.password(), user.getPassword());
        if (!normalPasswordMatched) {
            boolean temporaryPasswordMatched = temporaryPasswordStore.findValidEncodedPassword(user.getId())
                    .map(encoded -> passwordEncoder.matches(signInRequest.password(), encoded))
                    .orElse(false);

            if (!temporaryPasswordMatched) {
                throw new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS);
            }
        }

        return user;
    }

    @Transactional(readOnly = true)
    @Override
    public SignInResult signIn(SignInRequest signInRequest) {
        User user = validateSignIn(signInRequest);

        // 기존 로그인 강제 무효화
        tokenBlacklistService.setRevokeAllBefore(user.getId(), nowSeconds());

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getRole().name());

        JwtDto jwtDto = new JwtDto(mapToUserDto(user), accessToken);

        return new SignInResult(jwtDto, refreshToken);
    }

    @Transactional(readOnly = true)
    @Override
    public JwtDto refresh(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new AuthException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        UUID userId = jwtProvider.getUserId(refreshToken);
        String jti = jwtProvider.getTokenId(refreshToken);
        Instant iat = jwtProvider.getIssuedAt(refreshToken);

        if (tokenBlacklistService.isBlacklisted(jti)) {
            throw new AuthException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        Instant cutoff = tokenBlacklistService.getRevokeAllBefore(userId);
        if (cutoff != null && iat.isBefore(cutoff.truncatedTo(ChronoUnit.SECONDS))) {
            throw new AuthException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.AUTH_INVALID_TOKEN));

        if (user.isLocked()) {
            throw new UserException(ErrorCode.USER_ACCOUNT_LOCKED);
        }

        String newAccessToken = jwtProvider.generateAccessToken(userId, user.getRole().name());
        return new JwtDto(mapToUserDto(user), newAccessToken);
    }

    @Transactional(readOnly = true)
    @Override
    public void signOut(String accessToken, String refreshToken) {
        blacklistIfValid(accessToken);
        blacklistIfValid(refreshToken);
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

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = userRepository.findByEmail(resetPasswordRequest.email())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String tempPassword = generateTemporaryPassword();
        String encodedTempPassword = passwordEncoder.encode(tempPassword);

        temporaryPasswordStore.save(
                user.getId(),
                encodedTempPassword,
                Instant.now().plus(TEMP_PASSWORD_TTL)
        );

        tokenBlacklistService.setRevokeAllBefore(user.getId(), nowSeconds());

        // TODO: 메일 발송 연동
        log.info("[TEMP PASSWORD] userEmail={}", user.getEmail());
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

    private void blacklistIfValid(String token) {
        if (token == null || !jwtProvider.validateToken(token)) {
            return;
        }

        tokenBlacklistService.blacklist(jwtProvider.getTokenId(token), jwtProvider.getExpiration(token));

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
                .orElseThrow();

        return userMapper.toDto(user, name);
    }
}
