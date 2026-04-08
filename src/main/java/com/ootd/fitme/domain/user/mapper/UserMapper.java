package com.ootd.fitme.domain.user.mapper;

import com.ootd.fitme.domain.user.dto.request.UserCreateRequest;
import com.ootd.fitme.domain.user.dto.response.UserDto;
import com.ootd.fitme.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserCreateRequest userCreateRequest, String encodedPassword) {
        return User.create(userCreateRequest.email(), encodedPassword);
    }

    public UserDto toDto(User user, String name) {
        return new UserDto(
                user.getId(),
                user.getCreatedAt(),
                user.getEmail(),
                name,
                user.getRole(),
                user.isLocked()
        );
    }
}
