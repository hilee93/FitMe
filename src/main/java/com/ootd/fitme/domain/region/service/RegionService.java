package com.ootd.fitme.domain.region.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;

public interface RegionService {
    Region resolveAndUpsert(double longitude, double latitude);
    WeatherAPILocation resolveLocation(double longitude, double latitude);
}
