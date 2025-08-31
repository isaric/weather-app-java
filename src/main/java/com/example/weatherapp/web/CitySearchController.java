package com.example.weatherapp.web;

import com.example.weatherapp.city.CitySearchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CitySearchController {

    private final CitySearchService service;

    public CitySearchController(CitySearchService service) {
        this.service = service;
    }

    @GetMapping(value = "/api/cities/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> search(@RequestParam(name = "q", required = false) String query,
                               @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return service.searchSuggestions(query, safeLimit);
    }
}
