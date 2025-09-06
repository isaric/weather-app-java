package com.example.weatherapp.web;

import com.example.weatherapp.ai.AiSummaryService;
import com.example.weatherapp.weather.WeatherReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AiSummaryControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AiSummaryService aiSummaryService() {
            return Mockito.mock(AiSummaryService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiSummaryService aiSummaryService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        Mockito.reset(aiSummaryService);
        when(aiSummaryService.isConfigured()).thenReturn(true);
    }

    @Test
    public void summarizeShouldReturnAiSummary() throws Exception {
        // Create a sample WeatherReport
        WeatherReport.Hourly hourly = new WeatherReport.Hourly(
                List.of("2023-01-01T00:00", "2023-01-01T01:00"),
                List.of(20.5, 21.0),
                List.of(0.0, 0.0),
                List.of(5.0, 5.5),
                List.of(65.0, 70.0)
        );

        WeatherReport report = new WeatherReport(
                40.7128,
                -74.0060,
                "America/New_York",
                hourly
        );

        String expectedSummary = "Expect mild temperatures around 20-21°C with no precipitation.";
        when(aiSummaryService.summarize(any(WeatherReport.class), anyString(), anyString()))
                .thenReturn(expectedSummary);

        mockMvc.perform(post("/api/ai-summary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(report))
                .param("timezone", "America/New_York")
                .param("city", "New York"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary", is(expectedSummary)))
                .andExpect(jsonPath("$.model", is("gemini")))
                .andExpect(jsonPath("$.configured", is(true)));
    }
    
}
