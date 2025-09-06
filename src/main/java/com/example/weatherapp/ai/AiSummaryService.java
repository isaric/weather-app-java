package com.example.weatherapp.ai;

import com.example.weatherapp.weather.WeatherReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiSummaryService {

    private final String apiKey;
    private final String modelName;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile ChatLanguageModel cachedModel;

    public AiSummaryService(
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.gemini.model:gemini-1.5-flash}") String modelName
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelName = (modelName == null || modelName.isBlank()) ? "gemini-1.5-flash" : modelName;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String summarize(WeatherReport report, String timezone, String city) {
        if (!isConfigured()) {
            return "AI summary unavailable: missing Google Gemini API key.";
        }
        final var model = getModel();
        String reportJson;
        try {
            reportJson = mapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            reportJson = safeReport(report);
        }

        final var location = city != null && !city.isBlank() ? city : "the provided coordinates";
        final var prompt = getPrompt(timezone, location, reportJson);

        try {
            return model.generate(prompt);
        } catch (RuntimeException ex) {
            return "AI summary unavailable at the moment. Reason: " + ex.getMessage();
        }
    }

    @NotNull
    private static String getPrompt(String timezone, String location, String reportJson) {
        final var tz = timezone != null ? timezone : "auto";

        return "You are an assistant that summarizes short-term weather forecasts for lay people.\n" +
                "Given the JSON weather report from Open-Meteo (hourly arrays for next ~72 hours) and context, provide:\n" +
                "1) A concise overview of the upcoming weather for the next 1-3 days in " + location + " (timezone: " + tz + ").\n" +
                "2) Practical tips.\n" +
                "3) 3-5 activity suggestions suited to the conditions (indoor/outdoor).\n" +
                "Be specific about temperature ranges, precipitation likelihood, wind, and humidity.\n" +
                "Keep it under 180 words, use short paragraphs and bullet points.\n\n" +
                "Weather JSON:\n" + reportJson + "\n\n" +
                "Respond in plain text (no JSON).";
    }

    private String safeReport(WeatherReport r) {
        try { return mapper.writeValueAsString(r); } catch (Exception e) { return "{}"; }
    }

    private ChatLanguageModel getModel() {
        final var m = cachedModel;
        if (m == null) {
            synchronized (this) {
                final var m2 = cachedModel;
                if (m2 == null) {
                    cachedModel = GoogleAiGeminiChatModel.builder()
                            .apiKey(apiKey)
                            .modelName(modelName)
                            .build();
                }
                return cachedModel;
            }
        }
        return m;
    }
}
