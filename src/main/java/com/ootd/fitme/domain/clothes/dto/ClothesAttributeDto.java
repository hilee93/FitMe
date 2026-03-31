package com.ootd.fitme.domain.clothes.dto;

import java.util.UUID;

public record ClothesAttributeDto(
        UUID definitionId,
        String type) {
}
