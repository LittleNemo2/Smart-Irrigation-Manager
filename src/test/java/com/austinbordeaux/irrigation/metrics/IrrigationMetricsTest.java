package com.austinbordeaux.irrigation.metrics;

import com.austinbordeaux.irrigation.model.PlantType;
import com.austinbordeaux.irrigation.model.WateringEvent;
import com.austinbordeaux.irrigation.model.WateringOutcome;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.repository.ZoneRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IrrigationMetricsTest {

    private SimpleMeterRegistry registry;
    private IrrigationMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        ZoneRepository zones = mock(ZoneRepository.class);
        when(zones.countByOnlineTrue()).thenReturn(4L);
        metrics = new IrrigationMetrics(registry, zones);
    }

    private WateringEvent event(WateringOutcome outcome, double used, double baseline, boolean manual) {
        Zone zone = new Zone("Front Lawn", new PlantType("Cool-season turf", 0.80), 8.0, 20, 32);
        return new WateringEvent(zone, LocalDateTime.now(), outcome, 60, 20, used, baseline, 25, "test", manual);
    }

    @Test
    @DisplayName("Decisions are counted by outcome and trigger")
    void countsDecisionsByOutcomeAndTrigger() {
        metrics.recordDecision(event(WateringOutcome.WATERED, 160, 160, false));
        metrics.recordDecision(event(WateringOutcome.SKIPPED, 0, 160, false));
        metrics.recordDecision(event(WateringOutcome.SKIPPED, 0, 160, true));

        assertThat(registry.get("irrigation.decisions").tags("outcome", "watered", "trigger", "scheduled")
                .counter().count()).isEqualTo(1);
        assertThat(registry.get("irrigation.decisions").tags("outcome", "skipped", "trigger", "scheduled")
                .counter().count()).isEqualTo(1);
        assertThat(registry.get("irrigation.decisions").tags("outcome", "skipped", "trigger", "manual")
                .counter().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gallons used and saved are totaled per decision")
    void recordsWaterUsedAndSaved() {
        metrics.recordDecision(event(WateringOutcome.WATERED, 160, 160, false));
        metrics.recordDecision(event(WateringOutcome.SKIPPED, 0, 160, false));

        assertThat(registry.get("irrigation.water.used").summary().totalAmount()).isEqualTo(160);
        assertThat(registry.get("irrigation.water.saved").summary().totalAmount()).isEqualTo(160);
    }

    @Test
    @DisplayName("Weather cache hits and misses are counted separately")
    void countsCacheHitsAndMisses() {
        metrics.recordWeatherCache(true);
        metrics.recordWeatherCache(true);
        metrics.recordWeatherCache(false);

        assertThat(registry.get("weather.cache.requests").tag("result", "hit").counter().count()).isEqualTo(2);
        assertThat(registry.get("weather.cache.requests").tag("result", "miss").counter().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Weather fetches are timed and tagged by result")
    void timesWeatherFetches() {
        metrics.stopWeatherFetch(metrics.startWeatherFetch(), "success");
        metrics.stopWeatherFetch(metrics.startWeatherFetch(), "fallback");

        assertThat(registry.get("weather.fetch").tag("result", "success").timer().count()).isEqualTo(1);
        assertThat(registry.get("weather.fetch").tag("result", "fallback").timer().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The online-zones gauge reads from the repository")
    void reportsOnlineZones() {
        assertThat(registry.get("irrigation.zones.online").gauge().value()).isEqualTo(4);
    }
}
