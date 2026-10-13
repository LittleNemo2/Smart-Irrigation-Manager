package com.austinbordeaux.irrigation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "schedule")
public class Schedule extends BaseEntity {

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false, length = 80)
    private String daysOfWeek;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    protected Schedule() {
    }

    public Schedule(LocalTime startTime, Set<DayOfWeek> days, int durationMinutes) {
        this.startTime = startTime;
        setDays(days);
        this.durationMinutes = durationMinutes;
    }

    @Override
    public String getDisplayName() {
        return getDaysLabel() + " at " + startTime + " for " + durationMinutes + " min";
    }

    void assignZone(Zone zone) {
        this.zone = zone;
    }

    public boolean isDue(DayOfWeek day, LocalTime time) {
        return enabled
                && getDays().contains(day)
                && startTime.getHour() == time.getHour()
                && startTime.getMinute() == time.getMinute();
    }

    public int daysUntilNextRun(DayOfWeek today) {
        Set<DayOfWeek> days = getDays();
        for (int i = 1; i <= 7; i++) {
            if (days.contains(today.plus(i))) {
                return i;
            }
        }
        return 7;
    }

    public Set<DayOfWeek> getDays() {
        if (daysOfWeek == null || daysOfWeek.isBlank()) {
            return EnumSet.noneOf(DayOfWeek.class);
        }
        return Arrays.stream(daysOfWeek.split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }

    public final void setDays(Set<DayOfWeek> days) {
        this.daysOfWeek = days.stream()
                .sorted()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(","));
    }

    public String getDaysLabel() {
        Set<DayOfWeek> days = getDays();
        if (days.size() == 7) {
            return "Daily";
        }
        return days.stream()
                .sorted()
                .map(d -> d.getDisplayName(TextStyle.SHORT, Locale.US))
                .collect(Collectors.joining("/"));
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Zone getZone() {
        return zone;
    }
}