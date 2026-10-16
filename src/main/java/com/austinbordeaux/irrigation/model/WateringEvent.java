package com.austinbordeaux.irrigation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "watering_event")
public class WateringEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Zone zone;

    @Column(nullable = false)
    private LocalDateTime runAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private WateringOutcome outcome;

    @Column(nullable = false)
    private int probability;

    @Column(nullable = false)
    private int scheduledMinutes;

    @Column(nullable = false)
    private double gallonsUsed;

    @Column(nullable = false)
    private double baselineGallons;

    @Column(nullable = false)
    private double moistureBefore;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(nullable = false)
    private boolean manual;

    protected WateringEvent() {
    }

    public WateringEvent(Zone zone, LocalDateTime runAt, WateringOutcome outcome, int probability,
                         int scheduledMinutes, double gallonsUsed, double baselineGallons,
                         double moistureBefore, String reason, boolean manual) {
        this.zone = zone;
        this.runAt = runAt;
        this.outcome = outcome;
        this.probability = probability;
        this.scheduledMinutes = scheduledMinutes;
        this.gallonsUsed = gallonsUsed;
        this.baselineGallons = baselineGallons;
        this.moistureBefore = moistureBefore;
        this.reason = reason;
        this.manual = manual;
    }

    public double getGallonsSaved() {
        return baselineGallons - gallonsUsed;
    }

    @Override
    public String getDisplayName() {
        return zone.getName() + " " + outcome.getLabel().toLowerCase() + " at " + runAt;
    }

    public Zone getZone() {
        return zone;
    }

    public LocalDateTime getRunAt() {
        return runAt;
    }

    public WateringOutcome getOutcome() {
        return outcome;
    }

    public int getProbability() {
        return probability;
    }

    public int getScheduledMinutes() {
        return scheduledMinutes;
    }

    public double getGallonsUsed() {
        return gallonsUsed;
    }

    public double getBaselineGallons() {
        return baselineGallons;
    }

    public double getMoistureBefore() {
        return moistureBefore;
    }

    public String getReason() {
        return reason;
    }

    public boolean isManual() {
        return manual;
    }
}