package com.example.weatherapp.web;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // Use LangChain4j's PromptTemplate to generate a dynamic message without calling an external API
        PromptTemplate template = PromptTemplate.from("Welcome to Weather App, {{name}}! Today is {{date}}.");
        Prompt prompt = template.apply(Map.of(
                "name", "Traveler",
                "date", LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        ));

        model.addAttribute("message", prompt.text());
        model.addAttribute("today", LocalDate.now());
        return "index"; // Thymeleaf template located at src/main/resources/templates/index.html
    }
}
