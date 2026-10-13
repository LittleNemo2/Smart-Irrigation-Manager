package com.austinbordeaux.irrigation.model;

public enum Role {
    HOMEOWNER("Homeowner"),
    LANDSCAPER("Landscape business"),
    ADMIN("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}