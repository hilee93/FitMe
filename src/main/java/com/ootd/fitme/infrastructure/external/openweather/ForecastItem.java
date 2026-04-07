package com.ootd.fitme.infrastructure.external.openweather;

import java.util.List;

public record ForecastItem(
        Long dt, // 예보 시각 (date time)
        Main main, // 온도 / 습도 / 최저 / 최고 묶음
        List<Weather> weather,
        Wind wind,
        Double pop, // 강수확률 (probability of precipitation)
        Rain rain,
        Snow snow
) {
}
