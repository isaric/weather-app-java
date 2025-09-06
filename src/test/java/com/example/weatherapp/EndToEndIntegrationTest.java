package com.example.weatherapp;

import com.example.weatherapp.ai.AiSummaryService;
import com.example.weatherapp.city.CitySearchService;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EndToEndIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public CitySearchService citySearchService() {
            CitySearchService mockService = mock(CitySearchService.class);
            List<String> mockCities = List.of(
                    "New York US (40.7128,-74.006)",
                    "Newark US (40.7357,-74.1724)"
            );
            when(mockService.searchSuggestions(anyString(), anyInt())).thenReturn(mockCities);
            return mockService;
        }

        @Bean
        @Primary
        public AiSummaryService aiSummaryService() {
            AiSummaryService mockService = mock(AiSummaryService.class);
            when(mockService.isConfigured()).thenReturn(true);
            when(mockService.summarize(any(), anyString(), anyString()))
                    .thenReturn("Expect mild temperatures with no precipitation.");
            return mockService;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void fullUserJourney() throws Exception {
        // Step 1: User visits the home page
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeExists("today"));

        // Step 2: User searches for a city
        mockMvc.perform(get("/api/cities/search")
                .param("q", "new"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("New York US (40.7128,-74.006)")));

        // Step 3: User views the weather report for a city
        mockMvc.perform(get("/report")
                .param("lat", "40.7128")
                .param("lon", "-74.0060")
                .param("city", "New York"))
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attribute("lat", 40.7128))
                .andExpect(model().attribute("lon", -74.0060))
                .andExpect(model().attribute("city", "New York"));

        // Step 4: User requests an AI summary of the weather
        String weatherReportJson = """
                {
                  "latitude": 40.7128,
                  "longitude": -74.0060,
                  "timezone": "America/New_York",
                  "hourly": {
                    "time": ["2023-01-01T00:00", "2023-01-01T01:00"],
                    "temperature_2m": [20.5, 21.0],
                    "precipitation": [0.0, 0.0],
                    "wind_speed_10m": [5.0, 5.5],
                    "relative_humidity_2m": [65.0, 70.0]
                  }
                }""";

        mockMvc.perform(post("/api/ai-summary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(weatherReportJson)
                .param("timezone", "America/New_York")
                .param("city", "New York"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary", is("Expect mild temperatures with no precipitation.")))
                .andExpect(jsonPath("$.model", is("gemini")))
                .andExpect(jsonPath("$.configured", is(true)));
    }
}