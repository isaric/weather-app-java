package com.example.weatherapp.city;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Locale.ROOT;
import static java.util.Objects.compare;

@Service
public class CitySearchService {

    private static final String CLASSPATH_JSON = "cities.json";

    private final ReentrantLock lock = new ReentrantLock();
    private volatile List<City> cached;

    private final ObjectMapper mapper = new ObjectMapper();

    public List<String> searchSuggestions(String query, int limit) {
        if (query == null) return Collections.emptyList();
        final var q = query.trim().toLowerCase(ROOT);
        if (q.isEmpty()) return Collections.emptyList();

        final var all = loadAllCities();
        if (all.isEmpty()) return Collections.emptyList();

        final var matched = all.stream()
                .filter(c -> {
                    String name = safeLower(c.name());
                    String state = safeLower(c.state());
                    return (name != null && name.contains(q)) || (state != null && state.contains(q));
                })
                .limit(Math.max(limit, 10) * 10L).sorted((a, b) -> {
                    final var an = safeLower(a.name());
                    final var bn = safeLower(b.name());
                    final var as = startsWithScore(an, q) - startsWithScore(bn, q);
                    if (as != 0) return -as; // higher score first
                    final var al = an != null ? an.length() : MAX_VALUE;
                    final var bl = bn != null ? bn.length() : MAX_VALUE;
                    final var cmp = Integer.compare(al, bl);
                    if (cmp != 0) return cmp;
                    return compare(an, bn, String::compareTo);
                }).toList();

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
        return s == null ? null : s.toLowerCase(ROOT);
    }

    private List<City> loadAllCities() {
        final var local = cached;
        if (local != null) return local;
        lock.lock();
        try {
            if (cached != null) return cached;
            cached = tryLoadFromClasspath();
            return cached;
        } finally {
            lock.unlock();
        }
    }

    private List<City> tryLoadFromClasspath() {
        try {
            final var resource = new ClassPathResource(CLASSPATH_JSON);
            if (!resource.exists()) return Collections.emptyList();
            try (final var is = resource.getInputStream()) {
                return mapper.readValue(is, new TypeReference<>() {
                });
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
