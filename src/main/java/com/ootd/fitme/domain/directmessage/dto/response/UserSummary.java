package com.ootd.fitme.domain.directmessage.dto.response;

import java.util.UUID;

public record UserSummary(
        UUID userId,
        String name,
        String profileImageUrl
) {}
