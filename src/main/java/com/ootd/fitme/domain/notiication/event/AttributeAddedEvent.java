package com.ootd.fitme.domain.notiication.event;

import java.util.UUID;

public record AttributeAddedEvent(
        UUID userId,
        String attributeName
) {
}
