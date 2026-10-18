package com.austinbordeaux.irrigation.report;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsageReport extends Report {

    private final List<ZoneUsageRow> zones;
    private final ZoneUsageRow totals;

    public UsageReport(LocalDate from, LocalDate to, List<ZoneUsageRow> zones) {
        super("Water Usage Report", from, to);
        this.zones = List.copyOf(zones);
        this.totals = new ZoneUsageRow("All properties", "Total", "",
                zones.stream().mapToLong(ZoneUsageRow::watered).sum(),
                zones.stream().mapToLong(ZoneUsageRow::skipped).sum(),
                zones.stream().mapToDouble(ZoneUsageRow::gallonsUsed).sum(),
                zones.stream().mapToDouble(ZoneUsageRow::baselineGallons).sum());
    }

    @Override
    public List<String> headers() {
        return List.of("Property", "Zone", "Plant type", "Runs", "Watered", "Skipped",
                "Gallons used", "Fixed-timer gallons", "Gallons saved", "% saved");
    }

    @Override
    public List<List<String>> rows() {
        List<List<String>> rows = new ArrayList<>();
        for (ZoneUsageRow z : zones) {
            rows.add(toCells(z));
        }
        rows.add(toCells(totals));
        return rows;
    }

    private static List<String> toCells(ZoneUsageRow z) {
        return List.of(z.property(), z.zone(), z.plantType(),
                String.valueOf(z.runs()), String.valueOf(z.watered()), String.valueOf(z.skipped()),
                String.format(Locale.US, "%.0f", z.gallonsUsed()),
                String.format(Locale.US, "%.0f", z.baselineGallons()),
                String.format(Locale.US, "%.0f", z.gallonsSaved()),
                String.format(Locale.US, "%.1f", z.percentSaved()));
    }

    public List<ZoneUsageRow> getZones() {
        return zones;
    }

    public ZoneUsageRow getTotals() {
        return totals;
    }
}