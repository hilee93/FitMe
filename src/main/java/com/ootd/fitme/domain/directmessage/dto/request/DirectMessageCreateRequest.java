package com.ootd.fitme.domain.directmessage.dto.request;

import java.util.UUID;

public record DirectMessageCreateRequest(
        UUID receiverId,
        UUID senderId,
        String content
) {}
