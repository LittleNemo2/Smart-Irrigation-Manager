package com.austinbordeaux.irrigation.model;

public enum WateringOutcome {
    WATERED("Watered"),
    SKIPPED("Skipped");

    private final String label;

    WateringOutcome(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}