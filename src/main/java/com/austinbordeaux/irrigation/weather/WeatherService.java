package com.austinbordeaux.irrigation.weather;

import com.austinbordeaux.irrigation.metrics.IrrigationMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final Duration CACHE_TIME = Duration.ofMinutes(30);

    private final WeatherClient liveClient;
    private final WeatherClient fallbackClient;
    private final Map<String, WeatherReport> cache = new ConcurrentHashMap<>();
    private final IrrigationMetrics metrics;

    public WeatherService(OpenMeteoWeatherClient liveClient, SimulatedWeatherClient fallbackClient,
                          IrrigationMetrics metrics) {
        this.liveClient = liveClient;
        this.fallbackClient = fallbackClient;
        this.metrics = metrics;
    }

    public WeatherReport forecast(double latitude, double longitude) {
        String key = String.format(Locale.US, "%.2f,%.2f", latitude, longitude);
        WeatherReport cached = cache.get(key);
        if (cached != null && cached.fetchedAt().isAfter(LocalDateTime.now().minus(CACHE_TIME))) {
            metrics.recordWeatherCache(true);
            return cached;
        }
        metrics.recordWeatherCache(false);
        Timer.Sample sample = metrics.startWeatherFetch();
        try {
            WeatherReport fresh = liveClient.fetch(latitude, longitude);
            metrics.stopWeatherFetch(sample, "success");
            cache.put(key, fresh);
            return fresh;
        } catch (RuntimeException e) {
            metrics.stopWeatherFetch(sample, "fallback");
            log.warn("Open-Meteo unavailable ({}); using simulated weather", e.getMessage());
            return fallbackClient.fetch(latitude, longitude);
        }
    }
}
