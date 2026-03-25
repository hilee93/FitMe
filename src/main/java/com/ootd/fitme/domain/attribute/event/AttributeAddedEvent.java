package com.ootd.fitme.domain.attribute.event;

import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttributeAddedEvent(
        UUID attributeId,
        String attributeName,
        Instant createdAt
) {
}
