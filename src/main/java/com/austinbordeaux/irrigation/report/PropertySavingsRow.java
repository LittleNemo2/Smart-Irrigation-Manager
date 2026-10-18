package com.austinbordeaux.irrigation.report;

public record PropertySavingsRow(String property, long watered, long skipped, double gallonsUsed, double baselineGallons) {

    public double gallonsSaved() {
        return baselineGallons - gallonsUsed;
    }

    public double percentSaved() {
        return baselineGallons == 0 ? 0 : gallonsSaved() * 100 / baselineGallons;
    }
}