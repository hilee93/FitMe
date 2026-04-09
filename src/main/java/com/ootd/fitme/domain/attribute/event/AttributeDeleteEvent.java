package com.ootd.fitme.domain.attribute.event;

import com.ootd.fitme.domain.notification.enums.AttributeAction;

import java.time.Instant;
import java.util.UUID;

public record AttributeDeleteEvent(
        UUID attributeId,
        String attributeName,
        AttributeAction action,
        Instant createdAt
) {
}
