package com.example.weatherapp.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void reportShouldReturnReportTemplateWithValidCoordinates() throws Exception {
        mockMvc.perform(get("/report")
                .param("lat", "40.7128")
                .param("lon", "-74.0060")
                .param("city", "New York"))
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attributeExists("lat"))
                .andExpect(model().attributeExists("lon"))
                .andExpect(model().attributeExists("city"))
                .andExpect(model().attribute("lat", 40.7128))
                .andExpect(model().attribute("lon", -74.0060))
                .andExpect(model().attribute("city", "New York"));
    }

    @Test
    public void reportShouldWorkWithoutCityParameter() throws Exception {
        mockMvc.perform(get("/report")
                .param("lat", "40.7128")
                .param("lon", "-74.0060"))
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attributeExists("lat"))
                .andExpect(model().attributeExists("lon"))
                .andExpect(model().attribute("lat", 40.7128))
                .andExpect(model().attribute("lon", -74.0060));
    }

    @ParameterizedTest
    @CsvSource({
            "-91.0, 0.0",    // Latitude below -90
            "91.0, 0.0",     // Latitude above 90
            "0.0, -181.0",   // Longitude below -180
            "0.0, 181.0"     // Longitude above 180
    })
    public void reportShouldReturnErrorWithInvalidCoordinates(double lat, double lon) throws Exception {
        mockMvc.perform(get("/report")
                .param("lat", String.valueOf(lat))
                .param("lon", String.valueOf(lon)))
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Invalid coordinates."));
    }
}
