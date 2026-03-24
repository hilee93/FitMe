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

    public UserDto toDto(User user) {
        // TODO: profile 구현 후 프로필의 이름으로 매핑
        String fallbackName = user.getEmail() != null && user.getEmail().contains("@")
                ? user.getEmail().substring(0, user.getEmail().indexOf('@'))
                : "unknown";

        return new UserDto(
                user.getId(),
                user.getCreatedAt(),
                user.getEmail(),
                fallbackName,
                user.getRole(),
                user.isLocked()
        );
    }
}
