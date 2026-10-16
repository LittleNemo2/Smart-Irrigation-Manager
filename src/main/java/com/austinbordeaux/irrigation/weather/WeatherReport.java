package com.austinbordeaux.irrigation.weather;

import java.time.LocalDateTime;
import java.util.List;

// Current conditions plus a 7-day forecast for one location.
// live = true for real Open-Meteo data, false for simulated fallback data.
public record WeatherReport(double temperatureF,
                            int humidity,
                            double windMph,
                            int weatherCode,
                            List<DailyForecast> days,
                            LocalDateTime fetchedAt,
                            boolean live) {

    public String description() {
        return WeatherCodes.describe(weatherCode);
    }

    public DailyForecast today() {
        return days.get(0);
    }
}