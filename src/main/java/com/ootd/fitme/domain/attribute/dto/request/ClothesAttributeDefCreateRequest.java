package com.ootd.fitme.domain.attribute.dto.request;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
        String name,
        List<String> selectableValues
) {
}
