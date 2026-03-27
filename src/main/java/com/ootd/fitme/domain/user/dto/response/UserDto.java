package com.ootd.fitme.domain.user.dto.response;

import com.ootd.fitme.domain.user.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        Instant createdAt,
        String email,
        String name, // TODO: Profile 도메인 연동 후 사용자 이름으로 대체
        Role role,
        boolean locked
) {
}
