package com.example.weatherapp.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherReport {
    private Double latitude;
    private Double longitude;
    private String timezone;
    private Hourly hourly;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Hourly getHourly() {
        return hourly;
    }

    public void setHourly(Hourly hourly) {
        this.hourly = hourly;
    }

    // Nested class to match the "hourly" object structure
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hourly {
        private List<String> time;
        @JsonProperty("temperature_2m")
        private List<Double> temperature2m;
        private List<Double> precipitation;
        @JsonProperty("wind_speed_10m")
        private List<Double> windSpeed10m;
        @JsonProperty("relative_humidity_2m")
        private List<Double> relativeHumidity2m;

        public List<String> getTime() {
            return time;
        }

        public void setTime(List<String> time) {
            this.time = time;
        }

        public List<Double> getTemperature2m() {
            return temperature2m;
        }

        public void setTemperature2m(List<Double> temperature2m) {
            this.temperature2m = temperature2m;
        }

        public List<Double> getPrecipitation() {
            return precipitation;
        }

        public void setPrecipitation(List<Double> precipitation) {
            this.precipitation = precipitation;
        }

        public List<Double> getWindSpeed10m() {
            return windSpeed10m;
        }

        public void setWindSpeed10m(List<Double> windSpeed10m) {
            this.windSpeed10m = windSpeed10m;
        }

        public List<Double> getRelativeHumidity2m() {
            return relativeHumidity2m;
        }

        public void setRelativeHumidity2m(List<Double> relativeHumidity2m) {
            this.relativeHumidity2m = relativeHumidity2m;
        }
    }
}