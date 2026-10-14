package com.austinbordeaux.irrigation.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/weather")
    public String weather() {
        return "weather";
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    @GetMapping("/savings")
    public String savings() {
        return "savings";
    }
}