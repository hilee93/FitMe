package com.ootd.fitme.domain.user.repository;

import com.ootd.fitme.domain.user.dto.request.UserSearchCondition;
import com.ootd.fitme.domain.user.dto.response.CursorSlice;
import com.ootd.fitme.domain.user.dto.response.UserDto;

public interface UserRepositoryCustom {
    CursorSlice<UserDto> findUsersByCondition(UserSearchCondition condition);
}
