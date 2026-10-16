package com.austinbordeaux.irrigation.weather;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimulatedWeatherClient implements WeatherClient {

    @Override
    public WeatherReport fetch(double latitude, double longitude) {
        return reportStarting(LocalDate.now());
    }

    public WeatherReport reportStarting(LocalDate start) {
        List<DailyForecast> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(day(start.plusDays(i)));
        }
        DailyForecast first = days.get(0);
        return new WeatherReport(first.highF() - 6, 35, 8, first.weatherCode(),
                days, LocalDateTime.now(), false);
    }

    public DailyForecast day(LocalDate date) {
        int seed = date.getDayOfYear();
        double high = 62 + (seed * 7 % 15);
        double low = high - 28;
        int rainChance = (seed * 13) % 60;
        double rain = rainChance > 40 ? (rainChance - 40) * 0.01 : 0.0;
        double et0 = 0.08 + (seed % 5) * 0.02;
        int code = rain > 0 ? 61 : (rainChance > 25 ? 3 : 0);
        return new DailyForecast(date, high, low, rain, rainChance, et0, code);
    }
}