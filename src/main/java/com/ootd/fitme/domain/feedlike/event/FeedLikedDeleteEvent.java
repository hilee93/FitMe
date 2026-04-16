package com.ootd.fitme.domain.feedlike.event;

import java.util.UUID;

public record FeedLikedDeleteEvent(
        UUID feedLikeId,
        UUID feedId
) {
}
