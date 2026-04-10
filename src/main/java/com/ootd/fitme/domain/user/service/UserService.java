package com.ootd.fitme.domain.user.service;

import com.ootd.fitme.domain.user.dto.request.*;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.dto.response.UserDtoCursorResponse;

import java.util.UUID;

public interface UserService {
    String encodePassword(String password);
    boolean matchesPassword(String password, String encodedPassword);
    UserDto signUp(UserCreateRequest userCreateRequest);
    UserDto updateRole(UUID userId, UserRoleUpdateRequest userRoleUpdateRequest);
    UserDto updateLock(UUID userId, UserLockUpdateRequest userLockUpdateRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
    void changePassword(UUID userId, ChangePasswordRequest changePasswordRequest);
    UserDtoCursorResponse getUsers(UserSearchCondition userSearchCondition);
}
