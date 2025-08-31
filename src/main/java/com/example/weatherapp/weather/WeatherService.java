package com.example.weatherapp.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public WeatherReport fetchHourlyReport(double lat, double lon) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("hourly", String.join(",",
                        "temperature_2m",
                        "precipitation",
                        "wind_speed_10m",
                        "relative_humidity_2m"
                ))
                .queryParam("forecast_days", 3)
                .queryParam("timezone", "auto")
                .toUriString();

        ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
        Map body = resp.getBody();
        if (body == null) throw new IllegalStateException("Empty response from weather API");

        // Map response into our DTO
        WeatherReport report = new WeatherReport();
        report.setLatitude(asDouble(body.get("latitude")));
        report.setLongitude(asDouble(body.get("longitude")));
        report.setTimezone(asString(body.get("timezone")));

        Object hourlyObj = body.get("hourly");
        if (!(hourlyObj instanceof Map<?, ?> hourly)) {
            throw new IllegalStateException("Unexpected response: missing hourly");
        }
        report.setTimes(asStringList(hourly.get("time")));
        report.setTemperature2m(asDoubleList(hourly.get("temperature_2m")));
        report.setPrecipitation(asDoubleList(hourly.get("precipitation")));
        report.setWindSpeed10m(asDoubleList(hourly.get("wind_speed_10m")));
        report.setRelativeHumidity2m(asDoubleList(hourly.get("relative_humidity_2m")));

        return report;
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object o) {
        return (List<String>) o;
    }

    @SuppressWarnings("unchecked")
    private static List<Double> asDoubleList(Object o) {
        if (o == null) return null;
        return ((List<?>) o).stream().map(WeatherService::asDouble).toList();
    }

    private static Double asDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherReport {
        private Double latitude;
        private Double longitude;
        private String timezone;
        private List<String> times;
        @JsonProperty("temperature_2m")
        private List<Double> temperature2m;
        private List<Double> precipitation;
        @JsonProperty("wind_speed_10m")
        private List<Double> windSpeed10m;
        @JsonProperty("relative_humidity_2m")
        private List<Double> relativeHumidity2m;

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public List<String> getTimes() { return times; }
        public void setTimes(List<String> times) { this.times = times; }
        public List<Double> getTemperature2m() { return temperature2m; }
        public void setTemperature2m(List<Double> temperature2m) { this.temperature2m = temperature2m; }
        public List<Double> getPrecipitation() { return precipitation; }
        public void setPrecipitation(List<Double> precipitation) { this.precipitation = precipitation; }
        public List<Double> getWindSpeed10m() { return windSpeed10m; }
        public void setWindSpeed10m(List<Double> windSpeed10m) { this.windSpeed10m = windSpeed10m; }
        public List<Double> getRelativeHumidity2m() { return relativeHumidity2m; }
        public void setRelativeHumidity2m(List<Double> relativeHumidity2m) { this.relativeHumidity2m = relativeHumidity2m; }
    }
}
