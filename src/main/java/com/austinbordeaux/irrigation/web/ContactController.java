package com.austinbordeaux.irrigation.web;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.service.FeedbackService;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.web.form.ContactForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ContactController {

    private final FeedbackService feedbackService;
    private final PropertyService propertyService;

    public ContactController(FeedbackService feedbackService, PropertyService propertyService) {
        this.feedbackService = feedbackService;
        this.propertyService = propertyService;
    }

    @GetMapping("/contact")
    public String form(Principal principal, Model model) {
        AppUser user = propertyService.currentUser(principal);
        ContactForm form = new ContactForm();
        form.setName(user.getFullName());
        form.setEmail(user.getEmail());
        model.addAttribute("form", form);
        model.addAttribute("isAdmin", user.isAdmin());
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("form") ContactForm form, BindingResult result,
                         Principal principal, Model model, RedirectAttributes flash) {
        AppUser user = propertyService.currentUser(principal);
        if (result.hasErrors()) {
            model.addAttribute("isAdmin", user.isAdmin());
            return "contact";
        }
        feedbackService.submit(form, user.getUsername());
        flash.addFlashAttribute("success", "Thanks! Your message was sent.");
        return "redirect:/contact";
    }

    @GetMapping("/admin/feedback")
    public String inbox(Model model) {
        model.addAttribute("messages", feedbackService.all());
        return "admin/feedback";
    }
}