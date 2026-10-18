package com.austinbordeaux.irrigation.web;

import com.austinbordeaux.irrigation.model.AppUser;
import com.austinbordeaux.irrigation.report.SavingsReport;
import com.austinbordeaux.irrigation.service.PropertyService;
import com.austinbordeaux.irrigation.service.ReportService;
import com.austinbordeaux.irrigation.web.form.ReportRange;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Controller
@RequestMapping("/savings")
public class SavingsController {

    private final ReportService reportService;
    private final PropertyService propertyService;

    public SavingsController(ReportService reportService, PropertyService propertyService) {
        this.reportService = reportService;
        this.propertyService = propertyService;
    }

    @GetMapping
    public String savings(@Valid @ModelAttribute("range") ReportRange range, BindingResult result,
                          Principal principal, Model model) {
        if (!result.hasErrors()) {
            AppUser user = propertyService.currentUser(principal);
            model.addAttribute("report", reportService.savingsReport(user, range.getFrom(), range.getTo()));
        }
        return "savings";
    }

    @GetMapping("/savings.csv")
    public ResponseEntity<byte[]> savingsCsv(@Valid @ModelAttribute("range") ReportRange range,
                                             BindingResult result, Principal principal) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        AppUser user = propertyService.currentUser(principal);
        SavingsReport report = reportService.savingsReport(user, range.getFrom(), range.getTo());
        String filename = "water-savings-" + range.getFrom() + "-to-" + range.getTo() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(report.toCsv().getBytes(StandardCharsets.UTF_8));
    }
}