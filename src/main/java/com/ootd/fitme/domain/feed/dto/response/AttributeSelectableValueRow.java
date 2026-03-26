package com.ootd.fitme.domain.feed.dto.response;

import java.util.UUID;

public record AttributeSelectableValueRow(
        UUID attributeDefinitionId,
        String value
) {
}
