package com.ootd.fitme.domain.directmessage.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageDto(
        UUID id,
        Instant createdAt,
        UserSummary sender,
        UserSummary receiver,
        String content
) {}
