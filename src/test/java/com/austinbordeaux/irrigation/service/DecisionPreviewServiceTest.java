package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.engine.EvapotranspirationRule;
import com.austinbordeaux.irrigation.engine.IrrigationDecisionEngine;
import com.austinbordeaux.irrigation.engine.RainForecastRule;
import com.austinbordeaux.irrigation.engine.SoilMoistureRule;
import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.PlantType;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.Role;
import com.austinbordeaux.irrigation.model.Schedule;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.weather.DailyForecast;
import com.austinbordeaux.irrigation.weather.WeatherReport;
import com.austinbordeaux.irrigation.weather.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DecisionPreviewServiceTest {

    private final ZoneService zoneService = mock(ZoneService.class);
    private final WeatherService weatherService = mock(WeatherService.class);
    private final IrrigationDecisionEngine engine = new IrrigationDecisionEngine(
            List.of(new SoilMoistureRule(), new RainForecastRule(), new EvapotranspirationRule()));
    private final DecisionPreviewService service = new DecisionPreviewService(zoneService, weatherService, engine);

    private final AppUser owner = new AppUser("homeowner", "hash", "Hannah Homeowner", "h@example.com", Role.HOMEOWNER);
    private Zone zone;

    @BeforeEach
    void setUp() {
        Property property = new Property("Maple Street Home", "1420 Maple St", 43.13, -115.69, owner);
        zone = new Zone("Front Lawn", new PlantType("Cool-season turf", 0.80), 8.0, 20, 32);
        zone.addSchedule(new Schedule(LocalTime.of(5, 30), EnumSet.allOf(DayOfWeek.class), 20));
        property.addZone(zone);
        when(zoneService.get(1L, 2L, owner)).thenReturn(zone);
    }

    private WeatherReport weather(int rainChance, double rainInches, boolean live) {
        LocalDate today = LocalDate.now();
        return new WeatherReport(60, 40, 5, 0, List.of(
                new DailyForecast(today, 70, 45, rainInches, rainChance, 0.15, 0),
                new DailyForecast(today.plusDays(1), 70, 45, 0.0, 0, 0.15, 0)),
                LocalDateTime.now(), live);
    }

    @Test
    @DisplayName("Dry soil and a dry forecast preview as 'will water', with all three rules explained")
    void dryZonePreviewsWatering() {
        zone.setCurrentMoisture(15);
        when(weatherService.forecast(anyDouble(), anyDouble())).thenReturn(weather(0, 0.0, true));

        ZoneDecisionPreview preview = service.preview(1L, 2L, owner);

        assertThat(preview.zone()).isEqualTo("Front Lawn");
        assertThat(preview.property()).isEqualTo("Maple Street Home");
        assertThat(preview.willWater()).isTrue();
        assertThat(preview.probability()).isEqualTo(100);
        assertThat(preview.rules()).extracting("rule")
                .containsExactly("Soil moisture", "Rain forecast", "Evapotranspiration");
        assertThat(preview.liveWeather()).isTrue();
    }

    @Test
    @DisplayName("Wet soil with soaking rain coming previews as a skip and reports simulated weather")
    void wetZoneWithRainPreviewsSkip() {
        zone.setCurrentMoisture(30);
        when(weatherService.forecast(anyDouble(), anyDouble())).thenReturn(weather(90, 0.60, false));

        ZoneDecisionPreview preview = service.preview(1L, 2L, owner);

        assertThat(preview.willWater()).isFalse();
        assertThat(preview.level()).isEqualTo("low");
        assertThat(preview.liveWeather()).isFalse();
    }

    @Test
    @DisplayName("An offline zone previews as not running, with no rule breakdown")
    void offlineZoneDoesNotRun() {
        zone.setOnline(false);
        when(weatherService.forecast(anyDouble(), anyDouble())).thenReturn(weather(0, 0.0, true));

        ZoneDecisionPreview preview = service.preview(1L, 2L, owner);

        assertThat(preview.willWater()).isFalse();
        assertThat(preview.rules()).isEmpty();
        assertThat(preview.summary()).contains("offline");
    }

    @Test
    @DisplayName("A zone the user doesn't own is not found, and no weather is fetched")
    void otherUsersZoneIsNotFound() {
        AppUser stranger = new AppUser("landscaper", "hash", "Leo", "l@example.com", Role.LANDSCAPER);
        when(zoneService.get(1L, 2L, stranger)).thenThrow(new NotFoundException("Property", 1L));

        assertThatThrownBy(() -> service.preview(1L, 2L, stranger)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(weatherService);
    }
}
