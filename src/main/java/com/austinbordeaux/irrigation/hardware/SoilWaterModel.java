package com.austinbordeaux.irrigation.hardware;

import com.austinbordeaux.irrigation.model.Zone;

public final class SoilWaterModel {

    public static final double DRYING_PERCENT_PER_INCH = 110.0;  // plant water use
    public static final double RAIN_PERCENT_PER_INCH = 40.0;     // rain soaking in
    public static final double MIN_MOISTURE = 5.0;
    public static final double MAX_ABOVE_TARGET = 3.0;

    private SoilWaterModel() {
    }

    public static void dry(Zone zone, double et0Inches, double fractionOfDay) {
        double loss = et0Inches * zone.getPlantType().getCropCoefficient() * DRYING_PERCENT_PER_INCH * fractionOfDay;
        zone.setCurrentMoisture(Math.max(MIN_MOISTURE, zone.getCurrentMoisture() - loss));
    }

    public static void rain(Zone zone, double inches) {
        double gain = inches * RAIN_PERCENT_PER_INCH;
        zone.setCurrentMoisture(Math.min(zone.getTargetMoisture() + MAX_ABOVE_TARGET,
                zone.getCurrentMoisture() + gain));
    }
}