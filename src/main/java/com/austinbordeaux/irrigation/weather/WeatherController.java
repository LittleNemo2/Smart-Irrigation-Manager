package com.austinbordeaux.irrigation.web;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.weather.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class WeatherController {

    private final PropertyService propertyService;
    private final WeatherService weatherService;

    public WeatherController(PropertyService propertyService, WeatherService weatherService) {
        this.propertyService = propertyService;
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public String weather(@RequestParam(name = "propertyId", required = false) Long propertyId,
                          Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        List<Property> properties = propertyService.search(user, null);
        model.addAttribute("properties", properties);
        if (properties.isEmpty()) {
            return "weather";
        }
        Property selected = propertyId == null ? properties.get(0) : propertyService.get(propertyId, user);
        model.addAttribute("selected", selected);
        model.addAttribute("report", weatherService.forecast(selected.getLatitude(), selected.getLongitude()));
        return "weather";
    }
}