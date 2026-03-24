package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.auth.AuthException;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.mapper.UserMapper;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: 인증 관련 로직(signIn/토큰 발급)은 AuthService로 분리 검토
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;

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
    public JwtDto signIn(SignInRequest signInRequest) {
        User user = validateSignIn(signInRequest);

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        UserDto userDto = userMapper.toDto(user);

        return new JwtDto(userDto, accessToken);
    }
}
