package com.austinbordeaux.irrigation.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReportRange {

    public static final int MAX_DAYS = 366;

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