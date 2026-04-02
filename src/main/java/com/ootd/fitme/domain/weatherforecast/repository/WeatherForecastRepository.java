package com.ootd.fitme.domain.weatherforecast.repository;

import com.ootd.fitme.domain.weatherforecast.entity.WeatherForecast;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, UUID> {
    @Query("""
        select wf
        from WeatherForecast wf
        where wf.region.id = :regionId
          and wf.forecastedAt >= :baseTime
        order by wf.forecastedAt asc
        """)
    List<WeatherForecast> findUpcomingByRegionId(
            @Param("regionId") UUID regionId,
            @Param("baseTime") Instant baseTime,
            Pageable pageable
    );
}
