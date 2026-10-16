package com.austinbordeaux.irrigation.weather;

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

    public WeatherService(OpenMeteoWeatherClient liveClient, SimulatedWeatherClient fallbackClient) {
        this.liveClient = liveClient;
        this.fallbackClient = fallbackClient;
    }

    public WeatherReport forecast(double latitude, double longitude) {
        String key = String.format(Locale.US, "%.2f,%.2f", latitude, longitude);
        WeatherReport cached = cache.get(key);
        if (cached != null && cached.fetchedAt().isAfter(LocalDateTime.now().minus(CACHE_TIME))) {
            return cached;
        }
        try {
            WeatherReport fresh = liveClient.fetch(latitude, longitude);
            cache.put(key, fresh);
            return fresh;
        } catch (RuntimeException e) {
            log.warn("Open-Meteo unavailable ({}); using simulated weather", e.getMessage());
            return fallbackClient.fetch(latitude, longitude);
        }
    }
}
