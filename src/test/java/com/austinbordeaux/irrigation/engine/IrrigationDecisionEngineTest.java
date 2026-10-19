package com.austinbordeaux.irrigation.engine;

import com.austinbordeaux.irrigation.model.PlantType;
import com.austinbordeaux.irrigation.model.Schedule;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.weather.DailyForecast;
import com.austinbordeaux.irrigation.weather.WeatherReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IrrigationDecisionEngineTest {

    private final IrrigationDecisionEngine engine = new IrrigationDecisionEngine(
            List.of(new SoilMoistureRule(), new RainForecastRule(), new EvapotranspirationRule()));

    private Zone turfZone(double soilMoisture) {
        Zone zone = new Zone("Test Lawn", new PlantType("Cool-season turf", 0.80), 8.0, 20, 32);
        zone.addSchedule(new Schedule(LocalTime.of(5, 30), EnumSet.allOf(DayOfWeek.class), 20));
        zone.setCurrentMoisture(soilMoisture);
        return zone;
    }

    private WeatherReport weather(int rainChance, double rainInches, double et0Inches) {
        LocalDate today = LocalDate.now();
        DailyForecast day0 = new DailyForecast(today, 65, 40, rainInches, rainChance, et0Inches, 0);
        DailyForecast day1 = new DailyForecast(today.plusDays(1), 65, 40, 0.0, 0, et0Inches, 0);
        return new WeatherReport(60, 40, 5, 0, List.of(day0, day1), LocalDateTime.now(), false);
    }

    @Test
    @DisplayName("Dry soil with a dry, warm forecast waters")
    void drySoilAndDryForecastWaters() {
        Decision decision = engine.decide(turfZone(15), weather(0, 0.0, 0.15));
        assertTrue(decision.willWater());
        assertEquals(100, decision.probability());
    }

    @Test
    @DisplayName("Moist soil with a soaking rain coming skips")
    void moistSoilAndSoakingRainSkips() {
        Decision decision = engine.decide(turfZone(30), weather(90, 0.60, 0.04));
        assertFalse(decision.willWater());
        assertEquals(0, decision.probability());
    }

    @Test
    @DisplayName("An offline controller never runs")
    void offlineZoneNeverRuns() {
        Zone zone = turfZone(10);
        zone.setOnline(false);
        Decision decision = engine.decide(zone, weather(0, 0.0, 0.20));
        assertEquals(0, decision.probability());
        assertFalse(decision.willWater());
    }

    @Test
    @DisplayName("A zone whose only schedule is paused never runs")
    void pausedZoneNeverRuns() {
        Zone zone = turfZone(10);
        zone.getSchedules().get(0).setEnabled(false);
        Decision decision = engine.decide(zone, weather(0, 0.0, 0.20));
        assertEquals(0, decision.probability());
        assertEquals("No active schedule", decision.summary());
    }

    @Test
    @DisplayName("Each rule's adjustment is capped at ±50")
    void ruleAdjustmentIsCapped() {
        Decision decision = engine.decide(turfZone(5), weather(0, 0.0, 0.10));
        RuleResult soil = decision.results().get(0);
        assertEquals("Soil moisture", soil.rule());
        assertEquals(50, soil.adjustment());
    }

    @Test
    @DisplayName("Featured 1: a dry zone still waters when only drizzle is forecast")
    void dryZoneStillWatersWhenOnlyDrizzleIsForecast() {
        Decision decision = engine.decide(turfZone(15), weather(100, 0.05, 0.04));
        assertTrue(decision.willWater(),
                "Expected the dry zone to water, but probability was " + decision.probability() + "%");
    }
}