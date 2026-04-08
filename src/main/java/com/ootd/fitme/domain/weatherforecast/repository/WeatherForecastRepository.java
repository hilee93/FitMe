package com.ootd.fitme.domain.weatherforecast.repository;

import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, UUID> {

    boolean existsByRegionId(UUID regionId);

    Optional<WeatherForecast> findByRegionIdAndForecastAt(UUID regionId, Instant forecastAt);

    @Query("""
        select wf
        from WeatherForecast wf
        where wf.region.id = :regionId
          and wf.forecastAt < :forecastAt
        order by wf.forecastAt desc
        """)
    List<WeatherForecast> findBeforeForecastAt(
            @Param("regionId") UUID regionId,
            @Param("forecastAt") Instant forecastAt,
            Pageable pageable
    );

    @Query("""
        select wf
        from WeatherForecast wf
        where wf.region.id = :regionId
          and wf.forecastAt >= :baseTime
        order by wf.forecastAt asc
        """)
    List<WeatherForecast> findUpcomingByRegionId(
            @Param("regionId") UUID regionId,
            @Param("baseTime") Instant baseTime,
            Pageable pageable
    );

    default Optional<WeatherForecast> findLatestBeforeForecastAt(UUID regionId, Instant forecastAt) {
        return findBeforeForecastAt(regionId, forecastAt, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }
}
