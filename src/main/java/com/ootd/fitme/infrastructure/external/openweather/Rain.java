package com.ootd.fitme.infrastructure.external.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

// openweather 5day/3hour 응답
public record Rain(
        @JsonProperty("3h") Double volume3h
) {
}
