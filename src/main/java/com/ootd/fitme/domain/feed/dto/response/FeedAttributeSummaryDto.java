package com.ootd.fitme.domain.feed.dto.response;

import java.util.List;
import java.util.UUID;

public record FeedAttributeSummaryDto(
        UUID definitionId,
        String definitionName,
        List<String> selectableValues,
        String value
) {
}
