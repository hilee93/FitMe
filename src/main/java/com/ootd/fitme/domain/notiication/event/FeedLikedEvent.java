package com.ootd.fitme.domain.notiication.event;

import java.util.UUID;

public record FeedLikedEvent(
        UUID userId,
        String likerName
) {
}
