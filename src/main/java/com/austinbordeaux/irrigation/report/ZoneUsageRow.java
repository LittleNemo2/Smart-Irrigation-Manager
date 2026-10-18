package com.austinbordeaux.irrigation.report;

public record ZoneUsageRow(String property, String zone, String plantType,
                           long watered, long skipped,
                           double gallonsUsed, double baselineGallons) {

    public long runs() {
        return watered + skipped;
    }

    public double gallonsSaved() {
        return baselineGallons - gallonsUsed;
    }

    public double percentSaved() {
        return baselineGallons == 0 ? 0 : gallonsSaved() * 100 / baselineGallons;
    }
}