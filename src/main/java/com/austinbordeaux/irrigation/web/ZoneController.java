package com.austinbordeaux.irrigation.web;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.PlantType;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.service.ZoneService;
import com.austinbordeaux.irrigation.web.form.ZoneForm;
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
import java.util.List;

@Controller
@RequestMapping("/properties/{propertyId}/zones")
public class ZoneController {

    private final ZoneService zoneService;
    private final PropertyService propertyService;

    public ZoneController(ZoneService zoneService, PropertyService propertyService) {
        this.zoneService = zoneService;
        this.propertyService = propertyService;
    }

    @ModelAttribute("plantTypes")
    public List<PlantType> plantTypes() {
        return zoneService.plantTypes();
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long propertyId, Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        model.addAttribute("property", propertyService.get(propertyId, user));
        model.addAttribute("form", new ZoneForm());
        model.addAttribute("editing", false);
        return "zones/form";
    }

    @PostMapping
    public String create(@PathVariable Long propertyId, @Valid @ModelAttribute("form") ZoneForm form,
                         BindingResult result, Principal principal, Model model,
                         RedirectAttributes flash) {
        AppUser user = propertyService.currentUser(principal);
        if (result.hasErrors()) {
            model.addAttribute("property", propertyService.get(propertyId, user));
            model.addAttribute("editing", false);
            return "zones/form";
        }
        zoneService.create(propertyId, form, user);
        flash.addFlashAttribute("success", "Zone \"" + form.getName() + "\" was added.");
        return "redirect:/properties/" + propertyId;
    }

    @GetMapping("/{zoneId}/edit")
    public String editForm(@PathVariable Long propertyId, @PathVariable Long zoneId,
                           Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        model.addAttribute("property", propertyService.get(propertyId, user));
        model.addAttribute("form", ZoneForm.from(zoneService.get(propertyId, zoneId, user)));
        model.addAttribute("editing", true);
        model.addAttribute("zoneId", zoneId);
        return "zones/form";
    }

    @PostMapping("/{zoneId}")
    public String update(@PathVariable Long propertyId, @PathVariable Long zoneId,
                         @Valid @ModelAttribute("form") ZoneForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes flash) {
        AppUser user = propertyService.currentUser(principal);
        if (result.hasErrors()) {
            model.addAttribute("property", propertyService.get(propertyId, user));
            model.addAttribute("editing", true);
            model.addAttribute("zoneId", zoneId);
            return "zones/form";
        }
        zoneService.update(propertyId, zoneId, form, user);
        flash.addFlashAttribute("success", "Zone \"" + form.getName() + "\" was updated.");
        return "redirect:/properties/" + propertyId;
    }

    @PostMapping("/{zoneId}/delete")
    public String delete(@PathVariable Long propertyId, @PathVariable Long zoneId,
                         Principal principal, RedirectAttributes flash) {
        AppUser user = propertyService.currentUser(principal);
        String name = zoneService.get(propertyId, zoneId, user).getName();
        zoneService.delete(propertyId, zoneId, user);
        flash.addFlashAttribute("success", "Zone \"" + name + "\" was deleted.");
        return "redirect:/properties/" + propertyId;
    }
}