package com.austinbordeaux.irrigation.engine;

import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.weather.DailyForecast;

public record DecisionContext(Zone zone, double soilMoisture, DailyForecast today, DailyForecast tomorrow) {

    public double threshold() {
        return zone.getMoistureThreshold();
    }

    public double target() {
        return zone.getTargetMoisture();
    }

    public double cropCoefficient() {
        return zone.getPlantType().getCropCoefficient();
    }
}