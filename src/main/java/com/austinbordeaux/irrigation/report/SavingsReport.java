package com.austinbordeaux.irrigation.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SavingsReport extends Report {

    public static final double GOAL_PERCENT = 10.0;

    private final List<PropertySavingsRow> properties;
    private final PropertySavingsRow totals;
    private final double maxBaseline;

    public SavingsReport(LocalDate from, LocalDate to, List<PropertySavingsRow> properties) {
        super("Water Savings Report", from, to);
        this.properties = List.copyOf(properties);
        this.totals = new PropertySavingsRow("All properties",
                properties.stream().mapToLong(PropertySavingsRow::watered).sum(),
                properties.stream().mapToLong(PropertySavingsRow::skipped).sum(),
                properties.stream().mapToDouble(PropertySavingsRow::gallonsUsed).sum(),
                properties.stream().mapToDouble(PropertySavingsRow::baselineGallons).sum());
        this.maxBaseline = properties.stream().mapToDouble(PropertySavingsRow::baselineGallons).max().orElse(0);
    }

    public boolean isGoalMet() {
        return totals.percentSaved() >= GOAL_PERCENT;
    }

    public int baselineWidth(PropertySavingsRow row) {
        return width(row.baselineGallons());
    }

    public int usedWidth(PropertySavingsRow row) {
        return width(row.gallonsUsed());
    }

    private int width(double gallons) {
        return maxBaseline == 0 ? 0 : (int) Math.round(gallons * 100 / maxBaseline);
    }

    @Override
    public List<String> headers() {
        return List.of("Property", "Watered", "Skipped", "Fixed-timer gallons",
                "Smart gallons", "Gallons saved", "% saved");
    }

    @Override
    public List<List<String>> rows() {
        List<List<String>> rows = new ArrayList<>();
        for (PropertySavingsRow p : properties) {
            rows.add(toCells(p));
        }
        rows.add(toCells(totals));
        return rows;
    }

    private static List<String> toCells(PropertySavingsRow p) {
        return List.of(p.property(), String.valueOf(p.watered()), String.valueOf(p.skipped()),
                String.format(Locale.US, "%.0f", p.baselineGallons()),
                String.format(Locale.US, "%.0f", p.gallonsUsed()),
                String.format(Locale.US, "%.0f", p.gallonsSaved()),
                String.format(Locale.US, "%.1f", p.percentSaved()));
    }

    public List<PropertySavingsRow> getProperties() {
        return properties;
    }

    public PropertySavingsRow getTotals() {
        return totals;
    }
}