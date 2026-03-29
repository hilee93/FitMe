package com.ootd.fitme.domain.attribute.dto.request;

import java.util.List;

public record ClothesAttributeDefUpdateRequest(
        String name,
        List<String> selectableValues
) {
}
