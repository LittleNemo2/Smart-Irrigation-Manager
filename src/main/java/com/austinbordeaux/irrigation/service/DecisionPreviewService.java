package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.engine.Decision;
import com.austinbordeaux.irrigation.engine.IrrigationDecisionEngine;
import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.weather.WeatherReport;
import com.austinbordeaux.irrigation.weather.WeatherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Previews the watering decision for a zone without running the valve or saving a watering event.
 */
@Service
public class DecisionPreviewService {

    private final ZoneService zoneService;
    private final WeatherService weatherService;
    private final IrrigationDecisionEngine decisionEngine;

    public DecisionPreviewService(ZoneService zoneService, WeatherService weatherService,
                                  IrrigationDecisionEngine decisionEngine) {
        this.zoneService = zoneService;
        this.weatherService = weatherService;
        this.decisionEngine = decisionEngine;
    }

    @Transactional(readOnly = true)
    public ZoneDecisionPreview preview(Long propertyId, Long zoneId, AppUser user) {
        // ZoneService.get enforces ownership and throws NotFoundException for other users' zones
        Zone zone = zoneService.get(propertyId, zoneId, user);
        Property property = zone.getProperty();
        WeatherReport weather = weatherService.forecast(property.getLatitude(), property.getLongitude());
        Decision decision = decisionEngine.decide(zone, weather);

        return new ZoneDecisionPreview(
                zone.getId(),
                zone.getName(),
                property.getName(),
                decision.probability(),
                decision.willWater(),
                decision.level(),
                decision.summary(),
                decision.results(),
                weather.live(),
                LocalDateTime.now());
    }
}
