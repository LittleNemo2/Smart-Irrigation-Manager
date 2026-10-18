package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.WateringEvent;
import com.austinbordeaux.irrigation.model.WateringOutcome;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.report.UsageReport;
import com.austinbordeaux.irrigation.report.ZoneUsageRow;
import com.austinbordeaux.irrigation.repository.WateringEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.report.PropertySavingsRow;
import com.austinbordeaux.irrigation.report.SavingsReport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final WateringEventRepository eventRepository;

    public ReportService(WateringEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public UsageReport usageReport(AppUser user, LocalDate from, LocalDate to) {
        List<WateringEvent> events = eventRepository.search(user, user.isAdmin(), null, "", null,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        Map<Long, ZoneTotals> byZone = new LinkedHashMap<>();
        for (WateringEvent event : events) {
            Zone zone = event.getZone();
            ZoneTotals totals = byZone.computeIfAbsent(zone.getId(), id -> new ZoneTotals(zone));
            totals.add(event);
        }

        List<ZoneUsageRow> rows = new ArrayList<>();
        for (ZoneTotals totals : byZone.values()) {
            rows.add(totals.toRow());
        }
        rows.sort(Comparator.comparing(ZoneUsageRow::property).thenComparing(ZoneUsageRow::zone));
        return new UsageReport(from, to, rows);
    }
    public SavingsReport savingsReport(AppUser user, LocalDate from, LocalDate to) {
        List<WateringEvent> events = eventRepository.search(user, user.isAdmin(), null, "", null,
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());

        Map<Long, double[]> totals = new LinkedHashMap<>();   // [watered, skipped, used, baseline]
        Map<Long, String> names = new LinkedHashMap<>();
        for (WateringEvent event : events) {
            Property property = event.getZone().getProperty();
            names.putIfAbsent(property.getId(), property.getName());
            double[] t = totals.computeIfAbsent(property.getId(), id -> new double[4]);
            if (event.getOutcome() == WateringOutcome.WATERED) {
                t[0]++;
            } else {
                t[1]++;
            }
            t[2] += event.getGallonsUsed();
            t[3] += event.getBaselineGallons();
        }

        List<PropertySavingsRow> rows = new ArrayList<>();
        for (Map.Entry<Long, double[]> entry : totals.entrySet()) {
            double[] t = entry.getValue();
            rows.add(new PropertySavingsRow(names.get(entry.getKey()), (long) t[0], (long) t[1], t[2], t[3]));
        }
        rows.sort(Comparator.comparing(PropertySavingsRow::property));
        return new SavingsReport(from, to, rows);
    }
    private static final class ZoneTotals {
        private final Zone zone;
        private long watered;
        private long skipped;
        private double gallonsUsed;
        private double baselineGallons;

        private ZoneTotals(Zone zone) {
            this.zone = zone;
        }

        private void add(WateringEvent event) {
            if (event.getOutcome() == WateringOutcome.WATERED) {
                watered++;
            } else {
                skipped++;
            }
            gallonsUsed += event.getGallonsUsed();
            baselineGallons += event.getBaselineGallons();
        }

        private ZoneUsageRow toRow() {
            return new ZoneUsageRow(zone.getProperty().getName(), zone.getName(), zone.getPlantType().getName(),
                    watered, skipped, gallonsUsed, baselineGallons);
        }
    }
}