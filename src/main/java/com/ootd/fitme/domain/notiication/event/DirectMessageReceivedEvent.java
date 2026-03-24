package com.ootd.fitme.domain.notiication.event;

import java.util.UUID;

public record DirectMessageReceivedEvent(
        UUID receiverId,
        String senderName,
        String message
) {
}
