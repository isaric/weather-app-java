package com.example.weatherapp.city;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record City(
        @JsonProperty("name") String name,
        // two-letter country code expected
        @JsonProperty("country") String country,
        @JsonProperty("lat") Double lat,
        @JsonAlias({"lng", "lon"})
        @JsonProperty("lon") Double lon,
        // Optional field in some datasets (ignored for formatting)
        @JsonProperty("state") String state) {

    public String toSuggestionString() {
        String n = name != null ? name : "";
        String c = country != null ? country : "";
        String la = lat != null ? String.valueOf(trimDouble(lat)) : "";
        String lo = lon != null ? String.valueOf(trimDouble(lon)) : "";
        return String.format("%s %s (%s,%s)", n, c, la, lo).trim();
    }

    private static double trimDouble(Double d) {
        // limit to 6 decimal places in string form
        return Math.round(d * 1_000_000d) / 1_000_000d;
    }
}
