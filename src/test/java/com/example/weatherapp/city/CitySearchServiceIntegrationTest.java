package com.example.weatherapp.city;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class CitySearchServiceIntegrationTest {

    @Autowired
    private CitySearchService citySearchService;

    @Test
    public void searchShouldReturnEmptyListForNullQuery() {
        List<String> results = citySearchService.searchSuggestions(null, 10);
        assertThat(results).isEmpty();
    }

    @Test
    public void searchShouldReturnEmptyListForEmptyQuery() {
        List<String> results = citySearchService.searchSuggestions("", 10);
        assertThat(results).isEmpty();
    }

    @Test
    public void searchShouldReturnEmptyListForBlankQuery() {
        List<String> results = citySearchService.searchSuggestions("   ", 10);
        assertThat(results).isEmpty();
    }

    @Test
    public void searchShouldRespectLimitParameter() {
        int limit = 5;
        List<String> results = citySearchService.searchSuggestions("a", limit);
        
        assertThat(results.size()).isLessThanOrEqualTo(limit);

        if (!results.isEmpty()) {
            List<String> moreResults = citySearchService.searchSuggestions("a", limit * 2);
            assertThat(moreResults.size()).isGreaterThanOrEqualTo(results.size());
        }
    }

    @Test
    public void searchShouldHandleSpecialCharacters() {
        List<String> results = citySearchService.searchSuggestions(".*+?^${}()|[]\\", 10);
        assertThat(results).isEmpty();
    }

    @Test
    public void searchShouldOrderResultsCorrectly() {
        List<String> results = citySearchService.searchSuggestions("new", 10);

        if (!results.isEmpty()) {
            assertThat(results).allMatch(city ->
                city.toLowerCase().contains("new"));

            boolean anyStartsWithNew = results.stream()
                .anyMatch(city -> city.toLowerCase().startsWith("new"));
            
            if (anyStartsWithNew) {
                assertThat(results.getFirst().toLowerCase()).startsWith("new");
            }
        }
    }
}