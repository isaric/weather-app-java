package com.example.weatherapp.web;

import com.example.weatherapp.ai.AiSummaryService;
import com.example.weatherapp.weather.WeatherService.WeatherReport;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-summary")
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;

    public AiSummaryController(AiSummaryService aiSummaryService) {
        this.aiSummaryService = aiSummaryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> summarize(@RequestBody WeatherReport report,
                                       @RequestParam(value = "timezone", required = false) String timezone,
                                       @RequestParam(value = "city", required = false) String city) {
        try {
            String text = aiSummaryService.summarize(report, timezone, city);
            return ResponseEntity.ok(Map.of(
                    "summary", text,
                    "model", "gemini",
                    "configured", aiSummaryService.isConfigured()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "summary", "AI summary failed.",
                    "error", e.getMessage()
            ));
        }
    }
}
