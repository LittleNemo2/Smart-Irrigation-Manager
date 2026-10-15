package com.austinbordeaux.irrigation.web;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.service.ScheduleService;
import com.austinbordeaux.irrigation.service.ZoneService;
import com.austinbordeaux.irrigation.web.form.ScheduleForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.DayOfWeek;

@Controller
@RequestMapping("/properties/{propertyId}/zones/{zoneId}/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ZoneService zoneService;
    private final PropertyService propertyService;

    public ScheduleController(ScheduleService scheduleService, ZoneService zoneService,
                              PropertyService propertyService) {
        this.scheduleService = scheduleService;
        this.zoneService = zoneService;
        this.propertyService = propertyService;
    }

    @ModelAttribute("allDays")
    public DayOfWeek[] allDays() {
        return DayOfWeek.values();
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long propertyId, @PathVariable Long zoneId,
                          Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        model.addAttribute("zone", zoneService.get(propertyId, zoneId, user));
        model.addAttribute("form", new ScheduleForm());
        model.addAttribute("editing", false);
        return "schedules/form";
    }

    @PostMapping
    public String create(@PathVariable Long propertyId, @PathVariable Long zoneId,
                         @Valid @ModelAttribute("form") ScheduleForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes flash) {
        AppUser user = propertyService.currentUser(principal);
        if (result.hasErrors()) {
            model.addAttribute("zone", zoneService.get(propertyId, zoneId, user));
            model.addAttribute("editing", false);
            return "schedules/form";
        }
        scheduleService.create(propertyId, zoneId, form, user);
        flash.addFlashAttribute("success", "Schedule added.");
        return "redirect:/properties/" + propertyId;
    }

    @GetMapping("/{scheduleId}/edit")
    public String editForm(@PathVariable Long propertyId, @PathVariable Long zoneId,
                           @PathVariable Long scheduleId, Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        model.addAttribute("zone", zoneService.get(propertyId, zoneId, user));
        model.addAttribute("form", ScheduleForm.from(scheduleService.get(propertyId, zoneId, scheduleId, user)));
        model.addAttribute("editing", true);
        model.addAttribute("scheduleId", scheduleId);
        return "schedules/form";
    }

    @PostMapping("/{scheduleId}")
    public String update(@PathVariable Long propertyId, @PathVariable Long zoneId,
                         @PathVariable Long scheduleId,
                         @Valid @ModelAttribute("form") ScheduleForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes flash) {
        AppUser user = propertyService.currentUser(principal);
        if (result.hasErrors()) {
            model.addAttribute("zone", zoneService.get(propertyId, zoneId, user));
            model.addAttribute("editing", true);
            model.addAttribute("scheduleId", scheduleId);
            return "schedules/form";
        }
        scheduleService.update(propertyId, zoneId, scheduleId, form, user);
        flash.addFlashAttribute("success", "Schedule updated.");
        return "redirect:/properties/" + propertyId;
    }

    @PostMapping("/{scheduleId}/delete")
    public String delete(@PathVariable Long propertyId, @PathVariable Long zoneId,
                         @PathVariable Long scheduleId, Principal principal,
                         RedirectAttributes flash) {
        AppUser user = propertyService.currentUser(principal);
        scheduleService.delete(propertyId, zoneId, scheduleId, user);
        flash.addFlashAttribute("success", "Schedule deleted.");
        return "redirect:/properties/" + propertyId;
    }
}