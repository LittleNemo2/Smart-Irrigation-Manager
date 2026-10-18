package com.austinbordeaux.irrigation.web.form;

import com.austinbordeaux.irrigation.model.WateringOutcome;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HistoryFilter {

    public static final int MAX_DAYS = 366;

    private Long propertyId;

    @Size(max = 60, message = "Zone search must be 60 characters or fewer")
    @Pattern(regexp = "^[^<>]*$", message = "Zone search may not contain < or >")
    private String zone = "";

    private WateringOutcome outcome;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from = LocalDate.now().minusDays(30);

    @NotNull(message = "End date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to = LocalDate.now();

    @AssertTrue(message = "Start date must be on or before the end date")
    public boolean isDateOrderValid() {
        return from == null || to == null || !from.isAfter(to);
    }

    @AssertTrue(message = "Date range can be at most 366 days")
    public boolean isRangeValid() {
        return from == null || to == null || ChronoUnit.DAYS.between(from, to) <= MAX_DAYS;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone == null ? "" : zone.trim();
    }

    public WateringOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(WateringOutcome outcome) {
        this.outcome = outcome;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }
}