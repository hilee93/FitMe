package com.ootd.fitme.domain.weatherforecast.dto.response;

import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;

public record WindSpeedDto(
        Double speed,
        WindStrengthWord asWord
) {
}
