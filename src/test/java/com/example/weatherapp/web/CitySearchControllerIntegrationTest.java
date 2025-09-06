package com.example.weatherapp.web;

import com.example.weatherapp.city.CitySearchService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CitySearchControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public CitySearchService citySearchService() {
            return Mockito.mock(CitySearchService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CitySearchService citySearchService;

    @BeforeEach
    public void setup() {
        Mockito.reset(citySearchService);
    }

    @Test
    public void searchShouldReturnEmptyListForEmptyQuery() throws Exception {
        mockMvc.perform(get("/api/cities/search")
                .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void searchShouldReturnEmptyListForNullQuery() throws Exception {
        mockMvc.perform(get("/api/cities/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void searchShouldReturnCitiesForValidQuery() throws Exception {
        List<String> mockCities = Arrays.asList(
                "New York US (40.7128,-74.006)",
                "Newark US (40.7357,-74.1724)"
        );

        when(citySearchService.searchSuggestions(anyString(), anyInt()))
                .thenReturn(mockCities);

        mockMvc.perform(get("/api/cities/search")
                .param("q", "new"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("New York US (40.7128,-74.006)")))
                .andExpect(jsonPath("$[1]", is("Newark US (40.7357,-74.1724)")));
    }

    @Test
    public void searchShouldRespectLimitParameter() throws Exception {
        List<String> mockCities = Collections.singletonList(
                "New York US (40.7128,-74.006)"
        );

        when(citySearchService.searchSuggestions(anyString(), anyInt()))
                .thenReturn(mockCities);

        mockMvc.perform(get("/api/cities/search")
                .param("q", "new")
                .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", is("New York US (40.7128,-74.006)")));
    }
}
