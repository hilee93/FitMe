package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.request.UserLockUpdateRequest;
import com.ootd.fitme.domain.user.dto.request.UserRoleUpdateRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

// TODO: 인증 관련 로직(signIn/토큰 발급)은 AuthService로 분리 검토
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistService tokenBlacklistService;

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

        return userMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public User validateSignIn(SignInRequest signInRequest) {
        User user = userRepository.findByEmail(signInRequest.username())
                .orElseThrow(() -> new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        if (user.isLocked()) {
            throw new UserException(ErrorCode.USER_ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(signInRequest.password(), user.getPassword())) {
            throw new AuthException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        return user;
    }

    @Transactional(readOnly = true)
    @Override
    public SignInResult signIn(SignInRequest signInRequest) {
        User user = validateSignIn(signInRequest);

        // 기존 로그인 강제 무효화
        Instant cutoff = Instant.now();
        tokenBlacklistService.setRevokeAllBefore(user.getId(), cutoff);

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getRole().name());

        JwtDto jwtDto = new JwtDto(userMapper.toDto(user), accessToken);

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
        if (cutoff != null && iat.isBefore(cutoff)) {
            throw new AuthException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.AUTH_INVALID_TOKEN));

        if (user.isLocked()) {
            throw new UserException(ErrorCode.USER_ACCOUNT_LOCKED);
        }

        String newAccessToken = jwtProvider.generateAccessToken(userId, user.getRole().name());
        return new JwtDto(userMapper.toDto(user), newAccessToken);
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

        tokenBlacklistService.setRevokeAllBefore(userId, Instant.now());

        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public UserDto updateLock(UUID userId, UserLockUpdateRequest userLockUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        user.updateLocked(userLockUpdateRequest.locked());

        tokenBlacklistService.setRevokeAllBefore(userId, Instant.now());

        return userMapper.toDto(user);
    }

    private void blacklistIfValid(String token) {
        if (token == null || !jwtProvider.validateToken(token)) {
            return;
        }

        tokenBlacklistService.blacklist(jwtProvider.getTokenId(token), jwtProvider.getExpiration(token));

    }
}
