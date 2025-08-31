package com.example.weatherapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

    public ReportController() {
    }

    @GetMapping("/report")
    public String report(@RequestParam("lat") double lat,
                         @RequestParam("lon") double lon,
                         @RequestParam(value = "city", required = false) String city,
                         Model model) {
        // Basic validation of ranges
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            model.addAttribute("error", "Invalid coordinates.");
            return "report";
        }

        // Pass only basic info. Weather data will be fetched on the client side.
        model.addAttribute("city", city);
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        return "report";
    }
}
