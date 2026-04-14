package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import com.ootd.fitme.infrastructure.external.openweather.ForecastItem;

public final class WeatherConditionMapper {
    private WeatherConditionMapper() {
    }

    public static SkyStatus toSkyStatus(ForecastItem item) {
        int code = weatherCode(item);

        if (code == 800) {
            return SkyStatus.CLEAR;
        }

        if (code == 801 || code == 802) {
            return SkyStatus.MOSTLY_CLOUDY;
        }

        return SkyStatus.CLOUDY;
    }

    public static PrecipitationType toPrecipitationType(ForecastItem item) {
        int code = weatherCode(item);

        if (code >= 200 && code <= 400) {
            return PrecipitationType.SHOWER;
        }

        if (code >= 500 && code < 600) {
            return PrecipitationType.RAIN;
        }

        if (code == 611 || code == 612 || code == 613 || code == 615 || code == 616) {
            return PrecipitationType.RAIN_SNOW;
        }

        if (code >= 600 && code < 700) {
            return PrecipitationType.SNOW;
        }

        return PrecipitationType.NONE;
    }

    public static WindStrengthWord toWindStrength(double speed) {
        if (speed < 4.0) {
            return WindStrengthWord.WEAK;
        }

        if (speed < 9.0) {
            return WindStrengthWord.MODERATE;
        }

        return WindStrengthWord.STRONG;
    }

    public static double precipitationAmount(ForecastItem item) {
        double rain = item.rain() != null ? valueOrZero(item.rain().volume3h()) : 0.0;
        double snow = item.snow() != null ? valueOrZero(item.snow().volume3h()) : 0.0;
        return rain + snow;
    }

    // null value logic
    public static double valueOrZero(Double value) {

        return value != null ? value : 0.0;
    }

    private static int weatherCode(ForecastItem item) {
        if (item.weather() == null || item.weather().isEmpty() || item.weather().get(0).id() == null) {
            return 800;
        }
        return item.weather().get(0).id();
    }
}
