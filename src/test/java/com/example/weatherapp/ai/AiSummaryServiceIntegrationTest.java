package com.example.weatherapp.ai;

import com.example.weatherapp.weather.WeatherReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AiSummaryServiceIntegrationTest {

    @Autowired
    private AiSummaryService aiSummaryService;

    @Test
    public void isConfiguredShouldReturnTrueWhenApiKeyIsSet() {
        assertThat(aiSummaryService.isConfigured()).isTrue();
    }

    @Test
    public void isConfiguredShouldReturnFalseWhenApiKeyIsEmpty() {
        String originalApiKey = (String) ReflectionTestUtils.getField(aiSummaryService, "apiKey");
        try {
            ReflectionTestUtils.setField(aiSummaryService, "apiKey", "");
            assertThat(aiSummaryService.isConfigured()).isFalse();
        } finally {
            ReflectionTestUtils.setField(aiSummaryService, "apiKey", originalApiKey);
        }
    }

    @Test
    public void summarizeShouldReturnMessageWhenNotConfigured() {
        String originalApiKey = (String) ReflectionTestUtils.getField(aiSummaryService, "apiKey");
        try {
            ReflectionTestUtils.setField(aiSummaryService, "apiKey", "");
            
            WeatherReport report = createSampleWeatherReport();
            String summary = aiSummaryService.summarize(report, "UTC", "Test City");
            
            assertThat(summary).isEqualTo("AI summary unavailable: missing Google Gemini API key.");
        } finally {
            ReflectionTestUtils.setField(aiSummaryService, "apiKey", originalApiKey);
        }
    }

    private WeatherReport createSampleWeatherReport() {
        WeatherReport.Hourly hourly = new WeatherReport.Hourly(
                List.of("2023-01-01T00:00", "2023-01-01T01:00"),
                List.of(20.5, 21.0),
                List.of(0.0, 0.0),
                List.of(5.0, 5.5),
                List.of(65.0, 70.0)
        );

        return new WeatherReport(
                40.7128,
                -74.0060,
                "America/New_York",
                hourly
        );
    }
}