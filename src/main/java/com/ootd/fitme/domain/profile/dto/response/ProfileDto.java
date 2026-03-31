package com.ootd.fitme.domain.profile.dto.response;

import com.ootd.fitme.domain.profile.enums.Gender;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileDto(
        UUID userId,
        String name,
        Gender gender,
        LocalDate birthDate,
        WeatherAPILocation location,
        int temperatureSensitivity,
        String profileImageUrl
) {
}
