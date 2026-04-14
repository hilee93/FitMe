package com.ootd.fitme.domain.weatherforecast.service;

public record DailyTemperatureStat(
        double average,
        double minimum,
        double maximum
) {
    public static final DailyTemperatureStat ZERO = new DailyTemperatureStat(0.0, 0.0, 0.0);
}
