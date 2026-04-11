package com.ootd.fitme.domain.attribute.event;

import com.ootd.fitme.domain.notification.enums.AttributeAction;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttributeAddedEvent(
        UUID attributeId,
        String attributeName,
        AttributeAction action,
        Instant createdAt
) {
}
