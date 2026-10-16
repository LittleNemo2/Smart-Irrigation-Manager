package com.austinbordeaux.irrigation.weather;

import java.time.LocalDate;

public record DailyForecast(LocalDate date,
                            double highF,
                            double lowF,
                            double rainInches,
                            int rainChance,
                            double et0Inches,
                            int weatherCode) {

    public String description() {
        return WeatherCodes.describe(weatherCode);
    }
}