package com.ootd.fitme.domain.notiication.event;

import java.util.UUID;

public record FollowerNewFeedEvent(
        UUID userId,
        String writerName,
        String feedName
) {
}
