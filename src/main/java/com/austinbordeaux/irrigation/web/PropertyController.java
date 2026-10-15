package com.austinbordeaux.irrigation.web;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.service.PropertyService;
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

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
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
        model.addAttribute("property", propertyService.get(id, user));
        return "properties/detail";
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