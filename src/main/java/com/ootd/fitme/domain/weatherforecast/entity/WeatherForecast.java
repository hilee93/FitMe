package com.ootd.fitme.domain.weatherforecast.entity;

import com.ootd.fitme.domain.base.BaseEntity;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.weatherforecast.enums.PrecipitationType;
import com.ootd.fitme.domain.weatherforecast.enums.SkyStatus;
import com.ootd.fitme.domain.weatherforecast.enums.WindStrengthWord;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weather_forecast")
public class WeatherForecast extends BaseEntity {

    @Column(name = "forecasted_at", nullable = false)
    private Instant forecastedAt;

    @Column(name = "forecast_at", nullable = false)
    private Instant forecastAt;

    @Column(name = "sky_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SkyStatus skyStatus;

    @Column(name = "precipitation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PrecipitationType precipitationType;

    @Column(name = "precipitation_amount", nullable = false)
    private Double precipitationAmount;

    @Column(name = "precipitation_probability", nullable = false)
    private Double precipitationProbability;

    @Column(name = "humidity_current", nullable = false)
    private Double humidityCurrent;

    @Column(name = "humidity_compared_to_day_before", nullable = false)
    private Double humidityComparedToDayBefore;

    @Column(name = "temperature_current", nullable = false)
    private Double temperatureCurrent;

    @Column(name = "temperature_compared_to_day_before", nullable = false)
    private Double temperatureComparedToDayBefore;

    @Column(name = "temperature_min", nullable = false)
    private Double temperatureMin;

    @Column(name = "temperature_max", nullable = false)
    private Double temperatureMax;

    @Column(name = "wind_speed", nullable = false)
    private Double windSpeed;

    @Column(name = "wind_strength_word", nullable = false)
    @Enumerated(EnumType.STRING)
    private WindStrengthWord windStrengthWord;

    @JoinColumn(name = "region_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Region region;

    private WeatherForecast(
            Instant forecastedAt,
            Instant forecastAt,
            SkyStatus skyStatus,
            PrecipitationType precipitationType,
            Double precipitationAmount,
            Double precipitationProbability,
            Double humidityCurrent,
            Double humidityComparedToDayBefore,
            Double temperatureCurrent,
            Double temperatureComparedToDayBefore,
            Double temperatureMin,
            Double temperatureMax,
            Double windSpeed,
            WindStrengthWord windStrengthWord,
            Region region
    ) {
        this.forecastedAt = forecastedAt;
        this.forecastAt = forecastAt;
        this.skyStatus = skyStatus;
        this.precipitationType = precipitationType;
        this.precipitationAmount = precipitationAmount;
        this.precipitationProbability = precipitationProbability;
        this.humidityCurrent = humidityCurrent;
        this.humidityComparedToDayBefore = humidityComparedToDayBefore;
        this.temperatureCurrent = temperatureCurrent;
        this.temperatureComparedToDayBefore = temperatureComparedToDayBefore;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.windSpeed = windSpeed;
        this.windStrengthWord = windStrengthWord;
        this.region = region;
    }

    public static WeatherForecast create(
            Instant forecastedAt,
            Instant forecastAt,
            SkyStatus skyStatus,
            PrecipitationType precipitationType,
            Double precipitationAmount,
            Double precipitationProbability,
            Double humidityCurrent,
            Double humidityComparedToDayBefore,
            Double temperatureCurrent,
            Double temperatureComparedToDayBefore,
            Double temperatureMin,
            Double temperatureMax,
            Double windSpeed,
            WindStrengthWord windStrengthWord,
            Region region
    ) {
        return new WeatherForecast(
                forecastedAt,
                forecastAt,
                skyStatus,
                precipitationType,
                precipitationAmount,
                precipitationProbability,
                humidityCurrent,
                humidityComparedToDayBefore,
                temperatureCurrent,
                temperatureComparedToDayBefore,
                temperatureMin,
                temperatureMax,
                windSpeed,
                windStrengthWord,
                region
        );
    }

    public void updateFromCollection(
            Instant forecastedAt,
            SkyStatus skyStatus,
            PrecipitationType precipitationType,
            Double precipitationAmount,
            Double precipitationProbability,
            Double humidityCurrent,
            Double humidityComparedToDayBefore,
            Double temperatureCurrent,
            Double temperatureComparedToDayBefore,
            Double temperatureMin,
            Double temperatureMax,
            Double windSpeed,
            WindStrengthWord windStrengthWord
    ) {
        this.forecastedAt = forecastedAt;
        this.skyStatus = skyStatus;
        this.precipitationType = precipitationType;
        this.precipitationAmount = precipitationAmount;
        this.precipitationProbability = precipitationProbability;
        this.humidityCurrent = humidityCurrent;
        this.humidityComparedToDayBefore = humidityComparedToDayBefore;
        this.temperatureCurrent = temperatureCurrent;
        this.temperatureComparedToDayBefore = temperatureComparedToDayBefore;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.windSpeed = windSpeed;
        this.windStrengthWord = windStrengthWord;
    }
}
