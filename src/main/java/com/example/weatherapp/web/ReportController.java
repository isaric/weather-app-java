package com.example.weatherapp.web;

import com.example.weatherapp.weather.WeatherService;
import com.example.weatherapp.weather.WeatherService.WeatherReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

    private final WeatherService weatherService;
    private final ObjectMapper objectMapper;

    public ReportController(WeatherService weatherService) {
        this.weatherService = weatherService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/report")
    public String report(@RequestParam("lat") double lat,
                         @RequestParam("lon") double lon,
                         @RequestParam(value = "city", required = false) String city,
                         Model model) throws JsonProcessingException {
        // Basic validation of ranges
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            model.addAttribute("error", "Invalid coordinates.");
            return "report";
        }

        try {
            WeatherReport report = weatherService.fetchHourlyReport(lat, lon);
            String reportJson = objectMapper.writeValueAsString(report);

            model.addAttribute("city", city);
            model.addAttribute("lat", lat);
            model.addAttribute("lon", lon);
            model.addAttribute("timezone", report.getTimezone());
            model.addAttribute("reportJson", reportJson);
        } catch (Exception ex) {
            model.addAttribute("city", city);
            model.addAttribute("lat", lat);
            model.addAttribute("lon", lon);
            model.addAttribute("error", "Failed to fetch weather data. Please try again later.");
        }
        return "report";
    }
}
