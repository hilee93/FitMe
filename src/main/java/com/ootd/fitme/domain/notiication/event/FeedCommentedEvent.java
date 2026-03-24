package com.ootd.fitme.domain.notiication.event;

import java.util.UUID;

public record FeedCommentedEvent(
        UUID userId,
        String commenterName,
        String comment
) {
}
