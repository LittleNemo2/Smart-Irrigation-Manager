package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.metrics.IrrigationMetrics;
import com.austinbordeaux.irrigation.engine.Decision;
import com.austinbordeaux.irrigation.engine.IrrigationDecisionEngine;
import com.austinbordeaux.irrigation.hardware.SoilWaterModel;
import com.austinbordeaux.irrigation.hardware.ValveController;
import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.Schedule;
import com.austinbordeaux.irrigation.model.WateringEvent;
import com.austinbordeaux.irrigation.model.WateringOutcome;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.repository.WateringEventRepository;
import com.austinbordeaux.irrigation.repository.ZoneRepository;
import com.austinbordeaux.irrigation.weather.DailyForecast;
import com.austinbordeaux.irrigation.weather.SimulatedWeatherClient;
import com.austinbordeaux.irrigation.weather.WeatherReport;
import com.austinbordeaux.irrigation.weather.WeatherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class IrrigationService {

    private final IrrigationDecisionEngine decisionEngine;
    private final ValveController valveController;
    private final WateringEventRepository eventRepository;
    private final WeatherService weatherService;
    private final PropertyService propertyService;
    private final ZoneRepository zoneRepository;
    private final SimulatedWeatherClient simulatedWeather;
    private final IrrigationMetrics metrics;

    public IrrigationService(IrrigationDecisionEngine decisionEngine, ValveController valveController,
                             WateringEventRepository eventRepository, WeatherService weatherService,
                             PropertyService propertyService, ZoneRepository zoneRepository,
                             SimulatedWeatherClient simulatedWeather, IrrigationMetrics metrics) {
        this.decisionEngine = decisionEngine;
        this.valveController = valveController;
        this.eventRepository = eventRepository;
        this.weatherService = weatherService;
        this.propertyService = propertyService;
        this.zoneRepository = zoneRepository;
        this.simulatedWeather = simulatedWeather;
        this.metrics = metrics;
    }

    public WateringEvent runZone(Zone zone, WeatherReport weather, LocalDateTime when,
                                 int minutes, boolean manual) {
        double moistureBefore = zone.getCurrentMoisture();
        Decision decision = decisionEngine.decide(zone, weather);
        double baselineGallons = zone.gallonsFor(minutes);
        double gallonsUsed = 0;
        WateringOutcome outcome = WateringOutcome.SKIPPED;
        if (decision.willWater()) {
            gallonsUsed = valveController.run(zone, minutes);
            outcome = WateringOutcome.WATERED;
        }
        WateringEvent event = new WateringEvent(zone, when, outcome, decision.probability(), minutes,
                gallonsUsed, baselineGallons, moistureBefore, decision.summary(), manual);
        return eventRepository.save(event);
    }

    public int runCheckNow(Long propertyId, AppUser user) {
        Property property = propertyService.get(propertyId, user);
        WeatherReport weather = weatherService.forecast(property.getLatitude(), property.getLongitude());
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        int checked = 0;
        for (Zone zone : property.getZones()) {
            if (!zone.isOnline() || !zone.hasActiveSchedule()) {
                continue;
            }
            metrics.recordDecision(runZone(zone, weather, now, longestActiveMinutes(zone), true));
            checked++;
        }
        return checked;
    }

    public int runDueSchedules(LocalDateTime now) {
        LocalTime time = now.toLocalTime().withSecond(0).withNano(0);
        DayOfWeek day = now.getDayOfWeek();
        int ran = 0;
        for (Zone zone : zoneRepository.findAll()) {
            if (!zone.isOnline()) {
                continue;
            }
            for (Schedule schedule : zone.getSchedules()) {
                boolean due = schedule.isEnabled()
                        && schedule.getDays().contains(day)
                        && schedule.getStartTime().withSecond(0).withNano(0).equals(time);
                if (due) {
                    Property property = zone.getProperty();
                    WeatherReport weather = weatherService.forecast(property.getLatitude(), property.getLongitude());
                    metrics.recordDecision(runZone(zone, weather, now, schedule.getDurationMinutes(), false));
                    ran++;
                }
            }
        }
        return ran;
    }

    public void applyHourlyDrying() {
        for (Zone zone : zoneRepository.findAll()) {
            Property property = zone.getProperty();
            WeatherReport weather = weatherService.forecast(property.getLatitude(), property.getLongitude());
            SoilWaterModel.dry(zone, weather.today().et0Inches(), 1.0 / 24);
        }
    }

    public int backfillHistory(int days) {
        if (eventRepository.count() > 0) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        int created = 0;
        for (Zone zone : zoneRepository.findAll()) {
            if (!zone.isOnline()) {
                continue;
            }
            double currentMoisture = zone.getCurrentMoisture();
            for (int back = days; back >= 1; back--) {
                LocalDate date = today.minusDays(back);
                WeatherReport weather = simulatedWeather.reportStarting(date);
                DailyForecast dayWeather = weather.today();
                SoilWaterModel.dry(zone, dayWeather.et0Inches(), 1.0);
                SoilWaterModel.rain(zone, dayWeather.rainInches());
                for (Schedule schedule : zone.getSchedules()) {
                    if (schedule.isEnabled() && schedule.getDays().contains(date.getDayOfWeek())) {
                        runZone(zone, weather, date.atTime(schedule.getStartTime()),
                                schedule.getDurationMinutes(), false);
                        created++;
                    }
                }
            }
            zone.setCurrentMoisture(currentMoisture);
        }
        return created;
    }

    @Transactional(readOnly = true)
    public List<WateringEvent> recentFor(Property property) {
        return eventRepository.findTop10ByZone_PropertyOrderByRunAtDesc(property);
    }

    static int longestActiveMinutes(Zone zone) {
        return zone.getSchedules().stream()
                .filter(Schedule::isEnabled)
                .mapToInt(Schedule::getDurationMinutes)
                .max()
                .orElse(0);
    }
}