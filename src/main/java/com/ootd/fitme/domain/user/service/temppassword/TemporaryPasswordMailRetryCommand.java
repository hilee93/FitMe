package com.ootd.fitme.domain.user.service.temppassword;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record TemporaryPasswordMailRetryCommand(
        UUID userId,
        String toEmail,
        String temporaryPassword,
        Duration ttl,
        int attempt,
        Instant nextAttemptAt
) {
    public TemporaryPasswordMailRetryCommand withNextAttempt(Instant nextAttemptAt) {
        return new TemporaryPasswordMailRetryCommand(
                userId,
                toEmail,
                temporaryPassword,
                ttl,
                attempt + 1,
                nextAttemptAt
        );
    }
}
