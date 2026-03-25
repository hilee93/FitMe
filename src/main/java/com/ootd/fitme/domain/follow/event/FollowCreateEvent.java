package com.ootd.fitme.domain.follow.event;

import java.time.Instant;
import java.util.UUID;

public record FollowCreateEvent(
        UUID followId,
        UUID followeeId,
        UUID followerId,
        String followerName,
        Instant createAt
) {
}
