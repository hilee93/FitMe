package com.ootd.fitme.infrastructure.external.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Main(
        Double temp,
        @JsonProperty("temp_min") Double tempMin,
        @JsonProperty("temp_max") Double tempMax,
        Double humidity
) {
}
