package com.austinbordeaux.irrigation.web.form;

import com.austinbordeaux.irrigation.model.Schedule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class ScheduleForm {

    @NotNull(message = "Start time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotEmpty(message = "Pick at least one day")
    private Set<DayOfWeek> days = new LinkedHashSet<>();

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 240, message = "Duration must be 240 minutes or less")
    private Integer durationMinutes;

    private boolean enabled = true;

    public static ScheduleForm from(Schedule schedule) {
        ScheduleForm f = new ScheduleForm();
        f.startTime = schedule.getStartTime();
        f.days = new LinkedHashSet<>(schedule.getDays());
        f.durationMinutes = schedule.getDurationMinutes();
        f.enabled = schedule.isEnabled();
        return f;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public Set<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(Set<DayOfWeek> days) {
        this.days = days == null ? new LinkedHashSet<>() : days;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}