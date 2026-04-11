package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.exception.ProfileException;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.auth.AuthException;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.mapper.UserMapper;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.domain.user.service.temppassword.TemporaryPasswordStore;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.jwt.JwtProvider;
import com.ootd.fitme.global.security.jwt.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final TemporaryPasswordStore temporaryPasswordStore;
    private final UserMapper userMapper;
    private final ProfileRepository profileRepository;

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

    @Override
    @Transactional
    public SignInResult signIn(SignInRequest signInRequest) {
        User user = validateSignIn(signInRequest);

        tokenBlacklistService.setRevokeAllBefore(user.getId(), nowSeconds());

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getRole().name());

        JwtDto jwtDto = new JwtDto(mapToUserDto(user), accessToken);
        return new SignInResult(jwtDto, refreshToken);
    }

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

    @Override
    @Transactional
    public void signOut(String accessToken, String refreshToken) {
        blacklistIfValid(accessToken);
        blacklistIfValid(refreshToken);
    }

    private void blacklistIfValid(String token) {
        if (token == null || !jwtProvider.validateToken(token)) {
            return;
        }

        tokenBlacklistService.blacklist(jwtProvider.getTokenId(token), jwtProvider.getExpiration(token));
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
}
