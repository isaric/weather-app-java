package com.example.weatherapp.ai;

import com.example.weatherapp.weather.WeatherReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AiSummaryServiceIntegrationTest {

    @Autowired
    private AiSummaryService aiSummaryService;

    @Test
    public void isConfiguredShouldReturnTrueWhenApiKeyIsSet() {
        // The test profile sets a test API key
        assertThat(aiSummaryService.isConfigured()).isTrue();
    }

    @Test
    public void isConfiguredShouldReturnFalseWhenApiKeyIsEmpty() {
        // Temporarily set the API key to empty
        String originalApiKey = (String) ReflectionTestUtils.getField(aiSummaryService, "apiKey");
        try {
            ReflectionTestUtils.setField(aiSummaryService, "apiKey", "");
            assertThat(aiSummaryService.isConfigured()).isFalse();
        } finally {
            // Restore the original API key
            ReflectionTestUtils.setField(aiSummaryService, "apiKey", originalApiKey);
        }
    }

    @Test
    public void summarizeShouldReturnMessageWhenNotConfigured() {
        // Temporarily set the API key to empty
        String originalApiKey = (String) ReflectionTestUtils.getField(aiSummaryService, "apiKey");
        try {
            ReflectionTestUtils.setField(aiSummaryService, "apiKey", "");
            
            WeatherReport report = createSampleWeatherReport();
            String summary = aiSummaryService.summarize(report, "UTC", "Test City");
            
            assertThat(summary).isEqualTo("AI summary unavailable: missing Google Gemini API key.");
        } finally {
            // Restore the original API key
            ReflectionTestUtils.setField(aiSummaryService, "apiKey", originalApiKey);
        }
    }

    // Helper method to create a sample weather report for testing
    private WeatherReport createSampleWeatherReport() {
        WeatherReport report = new WeatherReport();
        report.setLatitude(40.7128);
        report.setLongitude(-74.0060);
        report.setTimezone("America/New_York");
        
        WeatherReport.Hourly hourly = new WeatherReport.Hourly();
        hourly.setTime(Arrays.asList("2023-01-01T00:00", "2023-01-01T01:00"));
        hourly.setTemperature2m(Arrays.asList(20.5, 21.0));
        hourly.setPrecipitation(Arrays.asList(0.0, 0.0));
        hourly.setWindSpeed10m(Arrays.asList(5.0, 5.5));
        hourly.setRelativeHumidity2m(Arrays.asList(65.0, 70.0));
        report.setHourly(hourly);
        
        return report;
    }
}