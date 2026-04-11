package com.ootd.fitme.domain.user.service.temppassword;

import java.time.Instant;

public record TempPasswordEntry(
        String encodedPassword,
        Instant expiresAt
) {
}
