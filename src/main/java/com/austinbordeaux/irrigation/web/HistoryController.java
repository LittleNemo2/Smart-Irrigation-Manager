package com.austinbordeaux.irrigation.web;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.model.WateringOutcome;
import com.austinbordeaux.irrigation.service.HistoryService;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.web.form.HistoryFilter;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@Controller
public class HistoryController {

    private final HistoryService historyService;
    private final PropertyService propertyService;

    public HistoryController(HistoryService historyService, PropertyService propertyService) {
        this.historyService = historyService;
        this.propertyService = propertyService;
    }

    @GetMapping("/history")
    public String history(@Valid @ModelAttribute("filter") HistoryFilter filter, BindingResult result,
                          Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        model.addAttribute("properties", propertyService.search(user, null));
        model.addAttribute("outcomes", WateringOutcome.values());
        if (!result.hasErrors()) {
            model.addAttribute("result", historyService.search(user, filter));
        }
        return "history";
    }
}