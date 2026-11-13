package com.austinbordeaux.irrigation.metrics;

import com.austinbordeaux.irrigation.model.WateringEvent;
import com.austinbordeaux.irrigation.repository.ZoneRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Custom application metrics, exported to Prometheus at /actuator/prometheus.
 *
 * <ul>
 *   <li>irrigation.decisions: watering decisions, tagged by outcome and trigger</li>
 *   <li>irrigation.water.used / irrigation.water.saved: gallons per decision</li>
 *   <li>irrigation.zones.online: zones whose controllers are online</li>
 *   <li>weather.fetch: Open-Meteo call latency, tagged by result</li>
 *   <li>weather.cache.requests: forecast cache hits and misses</li>
 * </ul>
 */
@Component
public class IrrigationMetrics {

    private final MeterRegistry registry;
    private final DistributionSummary waterUsed;
    private final DistributionSummary waterSaved;

    public IrrigationMetrics(MeterRegistry registry, ZoneRepository zoneRepository) {
        this.registry = registry;
        this.waterUsed = DistributionSummary.builder("irrigation.water.used")
                .description("Gallons used per watering decision")
                .baseUnit("gallons")
                .register(registry);
        this.waterSaved = DistributionSummary.builder("irrigation.water.saved")
                .description("Gallons saved compared with running the full schedule")
                .baseUnit("gallons")
                .register(registry);
        Gauge.builder("irrigation.zones.online", zoneRepository, ZoneRepository::countByOnlineTrue)
                .description("Zones whose controllers are online")
                .register(registry);
    }

    /** Records a live watering decision (scheduled or manual). Backfilled history is not recorded. */
    public void recordDecision(WateringEvent event) {
        Counter.builder("irrigation.decisions")
                .description("Watering decisions made by the decision engine")
                .tag("outcome", event.getOutcome().name().toLowerCase(Locale.ROOT))
                .tag("trigger", event.isManual() ? "manual" : "scheduled")
                .register(registry)
                .increment();
        waterUsed.record(event.getGallonsUsed());
        waterSaved.record(Math.max(0, event.getGallonsSaved()));
    }

    public Timer.Sample startWeatherFetch() {
        return Timer.start(registry);
    }

    /** @param result "success" when Open-Meteo answered, "fallback" when simulated weather was used */
    public void stopWeatherFetch(Timer.Sample sample, String result) {
        sample.stop(Timer.builder("weather.fetch")
                .description("Time spent fetching a forecast from Open-Meteo")
                .tag("result", result)
                .publishPercentileHistogram()
                .register(registry));
    }

    public void recordWeatherCache(boolean hit) {
        Counter.builder("weather.cache.requests")
                .description("Forecast cache lookups")
                .tag("result", hit ? "hit" : "miss")
                .register(registry)
                .increment();
    }
}
