package com.ootd.fitme.domain.weatherforecast.service;

import com.ootd.fitme.infrastructure.external.openweather.ForecastItem;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DailyTemperatureAggregator {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private DailyTemperatureAggregator() {
    }

    public static Map<LocalDate, DailyTemperatureStat> aggregate(
            List<ForecastItem> items,
            List<LocalDate> targetDates
    ) {
        Set<LocalDate> targetSet = new HashSet<>(targetDates);

        return items.stream()
                .filter(i -> i.dt() != null)
                .filter(i -> targetSet.contains(toKstDate(i.dt())))
                .collect(Collectors.groupingBy(
                        i -> toKstDate(i.dt()),
                        Collectors.collectingAndThen(Collectors.toList(), DailyTemperatureAggregator::toDailyTemperatureStat)
                ));
    }


    private static DailyTemperatureStat toDailyTemperatureStat(List<ForecastItem> dayItems) {
        if (dayItems == null || dayItems.isEmpty()) {
            return DailyTemperatureStat.ZERO;
        }

        double sum = 0.0;
        int count = 0;
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;

        for (ForecastItem item : dayItems) {
            if (item.main() == null) {
                continue;
            }

            double temp = WeatherConditionMapper.valueOrZero(item.main().temp());
            double slotMin = item.main().tempMin() != null ? item.main().tempMin() : temp;
            double slotMax = item.main().tempMax() != null ? item.main().tempMax() : temp;

            sum += temp;
            count++;
            minimum = Math.min(minimum, slotMin);
            maximum = Math.max(maximum, slotMax);
        }

        if (count == 0) {
            return DailyTemperatureStat.ZERO;
        }

        double average = sum / count;
        if (minimum == Double.POSITIVE_INFINITY) {
            minimum = average;
        }
        if (maximum == Double.NEGATIVE_INFINITY) {
            maximum = average;
        }

        return new DailyTemperatureStat(average, minimum, maximum);
    }

    private static LocalDate toKstDate(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond).atZone(KST).toLocalDate();
    }
}
