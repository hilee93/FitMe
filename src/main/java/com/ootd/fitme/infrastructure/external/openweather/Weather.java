package com.ootd.fitme.infrastructure.external.openweather;

public record Weather(
        Integer id,
        String main,
        String description
) {
}
