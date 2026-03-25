package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.user.dto.request.SignInRequest;
import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.response.JwtDto;
import com.ootd.fitme.domain.user.dto.response.SignInResult;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;

public interface UserService {
    String encodePassword(String password);
    boolean matchesPassword(String password, String encodedPassword);
    UserDto signUp(UserCreateRequest userCreateRequest);
    User validateSignIn(SignInRequest signInRequest);
    SignInResult signIn(SignInRequest signInRequest);
    JwtDto refresh(String refreshToken);
    void signOut(String accessToken, String refreshToken);
}
