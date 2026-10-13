package com.austinbordeaux.irrigation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "plant_type")
public class PlantType extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String name;

    @Column(nullable = false)
    private double cropCoefficient;

    protected PlantType() {
    }

    public PlantType(String name, double cropCoefficient) {
        this.name = name;
        this.cropCoefficient = cropCoefficient;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCropCoefficient() {
        return cropCoefficient;
    }

    public void setCropCoefficient(double cropCoefficient) {
        this.cropCoefficient = cropCoefficient;
    }
}