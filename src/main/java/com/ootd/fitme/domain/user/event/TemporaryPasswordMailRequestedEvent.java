package com.ootd.fitme.domain.user.event;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record TemporaryPasswordMailRequestedEvent(
        UUID userId,
        String toEmail,
        String temporaryPassword,
        Duration ttl,
        Instant requestedAt
) {
}
