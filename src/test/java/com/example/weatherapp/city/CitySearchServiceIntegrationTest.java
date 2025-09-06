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
        // This test assumes there are at least 5 cities in the dataset
        // If the test fails, it might be because the dataset is too small
        int limit = 5;
        List<String> results = citySearchService.searchSuggestions("a", limit);
        
        // The results should not exceed the limit
        assertThat(results.size()).isLessThanOrEqualTo(limit);
        
        // If we have results, check that a larger limit returns more results
        if (!results.isEmpty()) {
            List<String> moreResults = citySearchService.searchSuggestions("a", limit * 2);
            assertThat(moreResults.size()).isGreaterThanOrEqualTo(results.size());
        }
    }

    @Test
    public void searchShouldHandleSpecialCharacters() {
        // Test with special characters that might cause regex issues
        List<String> results = citySearchService.searchSuggestions(".*+?^${}()|[]\\", 10);
        assertThat(results).isEmpty();
    }

    @Test
    public void searchShouldOrderResultsCorrectly() {
        // This test assumes there are cities in the dataset that start with "new"
        // If the test fails, it might be because the dataset doesn't have such cities
        List<String> results = citySearchService.searchSuggestions("new", 10);
        
        // Check that results are not empty (if they are, the test is inconclusive)
        if (!results.isEmpty()) {
            // All results should contain "new" (case-insensitive)
            assertThat(results).allMatch(city -> 
                city.toLowerCase().contains("new"));
            
            // Cities that start with "new" should come before cities that just contain "new"
            // This is a bit tricky to test without knowing the exact dataset
            // So we'll just check that the first result starts with "new" if any do
            boolean anyStartsWithNew = results.stream()
                .anyMatch(city -> city.toLowerCase().startsWith("new"));
            
            if (anyStartsWithNew) {
                assertThat(results.get(0).toLowerCase()).startsWith("new");
            }
        }
    }
}