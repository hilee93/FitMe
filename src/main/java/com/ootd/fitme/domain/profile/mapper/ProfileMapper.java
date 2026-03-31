package com.ootd.fitme.domain.profile.mapper;

import com.ootd.fitme.domain.profile.dto.request.ProfileUpdateRequest;
import com.ootd.fitme.domain.profile.dto.response.ProfileDto;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class ProfileMapper {
    public ProfileDto toDto(Profile profile) {
        return new ProfileDto(
                profile.getUser().getId(),
                profile.getName(),
                profile.getGender(),
                profile.getBirthDate(),
                toLocation(profile),
                profile.getTemperatureSensitivity(),
                profile.getProfileImageUrl()
        );
    }

    public void apply(Profile profile, ProfileUpdateRequest request) {
        if (request == null) {
            return;
        }

        if (request.name() != null) {
            profile.updateName(request.name());
        }

        if (request.gender() != null) {
            profile.updateGender(request.gender());
        }

        if (request.birthDate() != null) {
            profile.updateBirthDate(request.birthDate());
        }

        if (request.temperatureSensitivity() != null) {
            profile.updateTemperatureSensitivity(request.temperatureSensitivity());
        }

        WeatherAPILocation location = request.location();

        if (location != null) {
            profile.updateLocation(
                    location.longitude(),
                    location.latitude(),
                    location.x(),
                    location.y(),
                    locationName(location.locationNames(),0),
                    locationName(location.locationNames(),1),
                    locationName(location.locationNames(),2)
            );
        }
    }

    private WeatherAPILocation toLocation(Profile profile) {
        boolean hasLocation = profile.getLatitude() != null
                || profile.getLongitude() != null
                || profile.getX() != null
                || profile.getY() != null
                || profile.getRegionOneDepthName() != null
                || profile.getRegionTwoDepthName() != null
                || profile.getRegionThreeDepthName() != null;

        if (!hasLocation) {
            return null;
        }

        List<String> locationNames = Stream.of(
                profile.getRegionOneDepthName(),
                profile.getRegionTwoDepthName(),
                profile.getRegionThreeDepthName()
        )
                .filter(Objects::nonNull)
                .toList();

        return new WeatherAPILocation(
                profile.getLatitude(),
                profile.getLongitude(),
                profile.getX(),
                profile.getY(),
                locationNames
        );
    }

    private String locationName(List<String> names, int index) {
        if (names == null || names.size() <= index) {
            return null;
        }

        String value = names.get(index);

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
