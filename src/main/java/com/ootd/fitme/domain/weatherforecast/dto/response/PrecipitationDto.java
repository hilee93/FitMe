package com.ootd.fitme.domain.weatherforecast.dto.response;

import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;

public record PrecipitationDto(
        PrecipitationType type,
        Double amount,
        Double probability
) {
}
