package com.example.weatherapp.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HomeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void homePageShouldReturnIndexTemplate() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeExists("today"));
    }

    @Test
    public void homePageShouldContainWelcomeMessage() throws Exception {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", containsString("Welcome to Weather App")))
                .andExpect(model().attribute("message", containsString(today)));
    }
}