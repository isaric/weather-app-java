package com.example.weatherapp.city;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CitySearchService {

    private static final String CLASSPATH_JSON = "city_search/cities.json";
    private static final String FALLBACK_URL = "https://raw.githubusercontent.com/isaric/weather-app/main/city_search/cities.json";

    private final Object lock = new Object();
    private volatile List<City> cached;

    private final ObjectMapper mapper = new ObjectMapper();

    public List<String> searchSuggestions(String query, int limit) {
        if (query == null) return Collections.emptyList();
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return Collections.emptyList();

        List<City> all = loadAllCities();
        if (all.isEmpty()) return Collections.emptyList();

        // Simple case-insensitive substring match against name and optionally state
        List<City> matched = all.stream()
                .filter(c -> {
                    String name = safeLower(c.getName());
                    String state = safeLower(c.getState());
                    return (name != null && name.contains(q)) || (state != null && state.contains(q));
                })
                .limit(Math.max(limit, 10) * 10L) // scan a bit more then trim to allow for simple ordering
                .collect(Collectors.toList());

        // Basic ordering: starts-with first, then by name length, then lexicographically
        matched.sort((a, b) -> {
            String an = safeLower(a.getName());
            String bn = safeLower(b.getName());
            int as = startsWithScore(an, q) - startsWithScore(bn, q);
            if (as != 0) return -as; // higher score first
            int al = an != null ? an.length() : Integer.MAX_VALUE;
            int bl = bn != null ? bn.length() : Integer.MAX_VALUE;
            int cmp = Integer.compare(al, bl);
            if (cmp != 0) return cmp;
            return Objects.compare(an, bn, String::compareTo);
        });

        return matched.stream()
                .map(City::toSuggestionString)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private static int startsWithScore(String s, String q) {
        if (s == null) return 0;
        return s.startsWith(q) ? 1 : 0;
    }

    private static String safeLower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }

    private List<City> loadAllCities() {
        List<City> local = cached;
        if (local != null) return local;
        synchronized (lock) {
            if (cached != null) return cached;
            List<City> loaded = tryLoadFromClasspath();
            if (loaded.isEmpty()) {
                loaded = tryLoadFromWeb();
            }
            cached = loaded;
            return cached;
        }
    }

    private List<City> tryLoadFromClasspath() {
        try {
            ClassPathResource resource = new ClassPathResource(CLASSPATH_JSON);
            if (!resource.exists()) return Collections.emptyList();
            try (InputStream is = resource.getInputStream()) {
                return mapper.readValue(is, new TypeReference<List<City>>() {});
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private List<City> tryLoadFromWeb() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(FALLBACK_URL))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                byte[] body = resp.body();
                if (body == null || body.length == 0) return Collections.emptyList();
                try (InputStream is = new java.io.ByteArrayInputStream(body)) {
                    return mapper.readValue(is, new TypeReference<List<City>>() {});
                }
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }
}
