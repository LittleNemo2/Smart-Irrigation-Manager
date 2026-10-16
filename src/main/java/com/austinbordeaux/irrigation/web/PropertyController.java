package com.austinbordeaux.irrigation.web;

import com.austinbordeaux.irrigation.engine.Decision;
import com.austinbordeaux.irrigation.engine.IrrigationDecisionEngine;
import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.Property;
import com.austinbordeaux.irrigation.model.Zone;
import com.austinbordeaux.irrigation.service.IrrigationService;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.weather.WeatherReport;
import com.austinbordeaux.irrigation.weather.WeatherService;
import com.austinbordeaux.irrigation.web.form.PropertyForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final WeatherService weatherService;
    private final IrrigationDecisionEngine decisionEngine;
    private final IrrigationService irrigationService;

    public PropertyController(PropertyService propertyService, WeatherService weatherService,
                              IrrigationDecisionEngine decisionEngine, IrrigationService irrigationService) {
        this.propertyService = propertyService;
        this.weatherService = weatherService;
        this.decisionEngine = decisionEngine;
        this.irrigationService = irrigationService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String q,
                       Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        model.addAttribute("properties", propertyService.search(user, q));
        model.addAttribute("q", q == null ? "" : q.trim());
        return "properties/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new PropertyForm());
        model.addAttribute("editing", false);
        return "properties/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PropertyForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("editing", false);
            return "properties/form";
        }
        propertyService.create(form, propertyService.currentUser(principal));
        flash.addFlashAttribute("success", "Property \"" + form.getName() + "\" was added.");
        return "redirect:/properties";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        Property property = propertyService.get(id, user);
        WeatherReport weather = weatherService.forecast(property.getLatitude(), property.getLongitude());
        Map<Long, Decision> decisions = new HashMap<>();
        for (Zone zone : property.getZones()) {
            decisions.put(zone.getId(), decisionEngine.decide(zone, weather));
        }
        model.addAttribute("property", property);
        model.addAttribute("weather", weather);
        model.addAttribute("decisions", decisions);
        model.addAttribute("events", irrigationService.recentFor(property));
        return "properties/detail";
    }

    @PostMapping("/{id}/run-check")
    public String runCheck(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        int checked = irrigationService.runCheckNow(id, propertyService.currentUser(principal));
        flash.addFlashAttribute("success", "Checked " + checked + " zone(s). See Recent activity below.");
        return "redirect:/properties/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        model.addAttribute("form", PropertyForm.from(propertyService.get(id, user)));
        model.addAttribute("editing", true);
        model.addAttribute("propertyId", id);
        return "properties/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") PropertyForm form,
                         BindingResult result, Principal principal, Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("editing", true);
            model.addAttribute("propertyId", id);
            return "properties/form";
        }
        propertyService.update(id, form, propertyService.currentUser(principal));
        flash.addFlashAttribute("success", "Property \"" + form.getName() + "\" was updated.");
        return "redirect:/properties/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        AppUser user = propertyService.currentUser(principal);
        String name = propertyService.get(id, user).getName();
        propertyService.delete(id, user);
        flash.addFlashAttribute("success", "Property \"" + name + "\" was deleted.");
        return "redirect:/properties";
    }
}