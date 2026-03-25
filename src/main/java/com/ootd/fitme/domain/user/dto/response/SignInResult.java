package com.ootd.fitme.domain.user.dto.response;

public record SignInResult(
        JwtDto jwtDto,
        String refreshToken
) {
}
