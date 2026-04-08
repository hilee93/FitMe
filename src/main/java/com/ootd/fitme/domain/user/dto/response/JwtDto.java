package com.ootd.fitme.domain.user.dto.response;

public record JwtDto(
        UserDto userDto,
        String accessToken
) {
}
