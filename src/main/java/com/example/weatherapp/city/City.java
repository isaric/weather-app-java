package com.example.weatherapp.city;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class City {

    private String name;
    // two-letter country code expected
    private String country;

    private Double lat;

    @JsonAlias({"lng", "lon"})
    @JsonProperty("lon")
    private Double lon;

    // Optional field in some datasets (ignored for formatting)
    private String state;

    public City() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

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
