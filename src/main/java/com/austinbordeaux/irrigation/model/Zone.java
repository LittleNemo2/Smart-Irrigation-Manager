package com.austinbordeaux.irrigation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "zone")
public class Zone extends BaseEntity {

    @Column(nullable = false, length = 60)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plant_type_id", nullable = false)
    private PlantType plantType;

    @Column(nullable = false)
    private double flowRateGpm;

    @Column(nullable = false)
    private double moistureThreshold;

    @Column(nullable = false)
    private double targetMoisture;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean online = true;

    @Column(nullable = false)
    private double currentMoisture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC")
    private List<Schedule> schedules = new ArrayList<>();

    protected Zone() {
    }

    public Zone(String name, PlantType plantType, double flowRateGpm,
                double moistureThreshold, double targetMoisture) {
        this.name = name;
        this.plantType = plantType;
        this.flowRateGpm = flowRateGpm;
        this.moistureThreshold = moistureThreshold;
        this.targetMoisture = targetMoisture;
        this.currentMoisture = (this.moistureThreshold + this.targetMoisture) / 2;
    }

    @Override
    public String getDisplayName() {
        return property == null ? name : property.getName() + " / " + name;
    }

    void assignProperty(Property property) {
        this.property = property;
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        schedule.assignZone(this);
    }

    public void removeSchedule(Schedule schedule) {
        schedules.remove(schedule);
    }

    public List<Schedule> getSchedules() {
        return Collections.unmodifiableList(schedules);
    }

    public double gallonsFor(double minutes) {
        return Math.max(0, minutes) * flowRateGpm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public void setPlantType(PlantType plantType) {
        this.plantType = plantType;
    }

    public double getFlowRateGpm() {
        return flowRateGpm;
    }

    public void setFlowRateGpm(double flowRateGpm) {
        this.flowRateGpm = flowRateGpm;
    }

    public double getMoistureThreshold() {
        return moistureThreshold;
    }

    public void setMoistureThreshold(double moistureThreshold) {
        this.moistureThreshold = moistureThreshold;
    }

    public double getTargetMoisture() {
        return targetMoisture;
    }

    public void setTargetMoisture(double targetMoisture) {
        this.targetMoisture = targetMoisture;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Property getProperty() {
        return property;
    }

    public boolean isOnline() { return online; }

    public void setOnline(boolean online) { this.online = online; }

    public double getCurrentMoisture() { return currentMoisture; }

    public void setCurrentMoisture(double currentMoisture) { this.currentMoisture = currentMoisture; }

    public boolean hasActiveSchedule() { return schedules.stream().anyMatch(Schedule::isEnabled); }

    public boolean isRunnable() { return online && hasActiveSchedule(); }

    public String getStatusLabel() { if (!online) { return "Offline"; }
        if (!hasActiveSchedule()) { return "Paused"; }
        return "Online";
    }

}
