package com.ootd.fitme.domain.attribute.dto.request;

import com.ootd.fitme.domain.clothes.enums.SortBy;
import com.ootd.fitme.domain.clothes.enums.SortDirection;

public record AttributeDefSearchCondition(
        SortBy sortBy,
        SortDirection sortDirection,
        String keywordLike
) {
}
