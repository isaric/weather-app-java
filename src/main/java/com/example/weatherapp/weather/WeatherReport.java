package com.example.weatherapp.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherReport(
        Double latitude,
        Double longitude,
        String timezone,
        Hourly hourly) {

    // Nested class to match the "hourly" object structure
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(
            List<String> time,
            @JsonProperty("temperature_2m") List<Double> temperature2m,
            List<Double> precipitation,
            @JsonProperty("wind_speed_10m") List<Double> windSpeed10m,
            @JsonProperty("relative_humidity_2m") List<Double> relativeHumidity2m) {
    }
}