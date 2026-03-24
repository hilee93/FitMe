package com.ootd.fitme.domain.notiication.event;

import java.util.UUID;

public record FollowedEvent(
        UUID userId,
        String followerName
) {
}
