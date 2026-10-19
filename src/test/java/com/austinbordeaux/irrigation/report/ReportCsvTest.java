package com.austinbordeaux.irrigation.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportCsvTest {

    @Test
    @DisplayName("A spreadsheet formula is neutralized (CSV injection)")
    void formulaIsNeutralized() {
        assertEquals("'=SUM(A1:A9)", Report.cell("=SUM(A1:A9)"));
    }

    @Test
    @DisplayName("Negative numbers are left alone")
    void negativeNumberIsUnchanged() {
        assertEquals("-12.5", Report.cell("-12.5"));
    }

    @Test
    @DisplayName("Values containing commas are quoted")
    void commaValueIsQuoted() {
        assertEquals("\"Smith, Jr\"", Report.cell("Smith, Jr"));
    }

    @Test
    @DisplayName("Usage report totals add up and appear in the CSV")
    void usageReportTotalsAddUp() {
        ZoneUsageRow lawn = new ZoneUsageRow("Maple", "Lawn", "Turf", 3, 1, 480, 640);
        ZoneUsageRow garden = new ZoneUsageRow("Maple", "Garden", "Garden", 2, 2, 80, 160);
        UsageReport report = new UsageReport(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7),
                List.of(lawn, garden));

        assertEquals(560, report.getTotals().gallonsUsed(), 0.001);
        assertEquals(800, report.getTotals().baselineGallons(), 0.001);
        assertEquals(30.0, report.getTotals().percentSaved(), 0.001);
        assertTrue(report.toCsv().contains("All properties,Total,,8,5,3,560,800,240,30.0"));
    }
}