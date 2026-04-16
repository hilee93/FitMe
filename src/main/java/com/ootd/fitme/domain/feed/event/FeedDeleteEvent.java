package com.ootd.fitme.domain.feed.event;

import java.util.UUID;

public record FeedDeleteEvent(
        UUID feedId
) {
}
