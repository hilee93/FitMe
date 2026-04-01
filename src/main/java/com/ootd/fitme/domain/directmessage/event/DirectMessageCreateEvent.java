package com.ootd.fitme.domain.directmessage.event;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageCreateEvent(
        UUID messageId,
        UUID receiverId,
        UUID senderId,
        String senderName,
        String senderProfileImageUrl,
        String receiverName,
        String receiverProfileImageUrl,
        String message,
        Instant createdAt
) {
}
