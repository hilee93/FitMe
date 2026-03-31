package com.ootd.fitme.domain.profile.dto.request;

import com.ootd.fitme.domain.profile.enums.Gender;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        @Size(max = 50)
        String name,

        Gender gender,
        LocalDate birthDate,
        WeatherAPILocation location,

        @Min(1)
        @Max(5)
        Integer temperatureSensitivity
) {
}
