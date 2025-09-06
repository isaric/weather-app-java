Project: Weather App Java – Development Guidelines (Advanced)

This document captures project-specific knowledge for advanced development, build, testing, and troubleshooting.

1. Build and Configuration
- Toolchain
  - Java: 21 (configured via Gradle Toolchains). Ensure a JDK 21 is available; Gradle will auto-provision if required.
  - Build: Gradle with Spring Boot plugin 3.5.5 and io.spring.dependency-management 1.1.7.
- Dependencies (high-level)
  - Web/UI: spring-boot-starter-web, spring-boot-starter-thymeleaf
  - AI: langchain4j 0.35.0, langchain4j-google-ai-gemini 0.35.0
  - Tests: spring-boot-starter-test (JUnit 5 / Jupiter)
- Build commands
  - Full build (no tests): ./gradlew assemble
  - Build with tests: ./gradlew build
  - Run app locally: ./gradlew bootRun
  - Docker: docker build -t weather-app-java . && docker run -p 8080:8080 -e AI_API_KEY=... weather-app-java
- Runtime configuration
  - AI config (Google Gemini via Google AI Studio):
    - Property: ai.gemini.api-key (String). If blank/missing, AI summary endpoints respond with fallback message and UI shows unavailable summary.
    - Property: ai.gemini.model (String). Default is gemini-1.5-flash; empty/blank coerced to default.
  - How to set:
    - application.properties (src/main/resources) or runtime environment e.g., -Dai.gemini.api-key=... or SPRING_APPLICATION_JSON; README shows examples too.
  - Ports: Spring Boot default 8080. Override with server.port if needed.
  - Data: City suggestions are loaded from src/main/resources/cities.json in CitySearchService.
- Devcontainer
  - .devcontainer/devcontainer.json exists (if using VS Code / JetBrains Gateway). It provides a consistent dev environment; match Java 21.

2. Testing – Running, Adding, and Examples
- Test profile
  - Active profile for tests: "test" (via @ActiveProfiles("test") in several integration tests).
  - src/test/resources/application-test.properties sets ai.gemini.api-key=test-key to avoid real external calls. AI beans can be further overridden/mocked within @TestConfiguration in tests where needed (see EndToEndIntegrationTest and web/AiSummaryControllerIntegrationTest for patterns).
- Commands
  - Run all tests: ./gradlew test
  - Run a single class: ./gradlew test --tests "com.example.weatherapp.web.ReportControllerIntegrationTest"
  - Run a single method: ./gradlew test --tests "com.example.weatherapp.web.ReportControllerIntegrationTest.methodName"
  - Show stacktraces: add -i or --stacktrace
  - Continuous testing (watch mode): ./gradlew test --continuous (re-runs on changes)
- Test types present
  - Controller Integration Tests (MockMvc):
    - web/CitySearchControllerIntegrationTest
    - web/ReportControllerIntegrationTest
    - web/HomeControllerIntegrationTest
    - web/AiSummaryControllerIntegrationTest (uses @TestConfiguration to override AiSummaryService with a mock)
  - Service Integration Tests:
    - city/CitySearchServiceIntegrationTest
    - ai/AiSummaryServiceIntegrationTest
  - End-to-End Integration Test:
    - EndToEndIntegrationTest bootstraps the full Spring context, overrides both CitySearchService and AiSummaryService via @TestConfiguration, and exercises a realistic flow: GET /, city search endpoint, /report page, then POST /api/ai-summary with a weather report JSON payload. This is a good blueprint for authoring system-level tests.
- How to add a new test
  - Place tests under src/test/java mirroring main package structure.
  - For Spring tests, annotate with @SpringBootTest (or @WebMvcTest for slice tests) and @AutoConfigureMockMvc if you need MockMvc.
  - If the test should avoid external AI calls, either rely on the test profile (ai.gemini.api-key=test-key ensures AiSummaryService.isConfigured() true) or provide a @TestConfiguration with a @Primary bean overriding AiSummaryService to a Mockito mock and stub summarize(). Pattern:

    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    class MyControllerIT {
      @TestConfiguration
      static class Cfg {
        @Bean @Primary AiSummaryService ai() {
          var mock = org.mockito.Mockito.mock(AiSummaryService.class);
          org.mockito.Mockito.when(mock.isConfigured()).thenReturn(true);
          org.mockito.Mockito.when(mock.summarize(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
            .thenReturn("Stubbed summary");
          return mock;
        }
      }
      @Autowired MockMvc mvc;
      @Test void responds200() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/"))
           .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
      }
    }

  - Run only this test to validate quickly: ./gradlew test --tests "com.example.weatherapp.MyControllerIT"
- Creating and running a simple test example that works
  - Use any of the existing tests as a working example; they build and run with the provided test profile. For a minimal sanity test that does not require the web context:

    package com.example.weatherapp;

    import org.junit.jupiter.api.Test;
    import static org.junit.jupiter.api.Assertions.*;

    class SanityTest {
      @Test void sanity() {
        assertTrue(21 >= 17, "Java toolchain is at least 17");
      }
    }

  - Place it at src/test/java/com/example/weatherapp/SanityTest.java.
  - Run: ./gradlew test --tests "com.example.weatherapp.SanityTest".
  - Note: This example is not committed to the repository per instructions; create locally if needed and delete afterward.

3. Project Architecture and Development Notes
- Packages
  - com.example.weatherapp.web – Controllers for endpoints and pages (Thymeleaf views: templates/index.html, templates/report.html).
  - com.example.weatherapp.city – CitySearchService uses cities.json; exposes searchSuggestions(query, limit) used by controller and tests.
  - com.example.weatherapp.ai – AiSummaryService integrates LangChain4j Google Gemini model; designed to degrade gracefully when not configured.
  - com.example.weatherapp.weather – WeatherReport is the domain model consumed by the AI summary endpoint.
- AiSummaryService specifics
  - Configuration: ai.gemini.api-key and ai.gemini.model (default "gemini-1.5-flash").
  - Behavior:
    - isConfigured(): returns true if api key is non-blank.
    - summarize(report, timezone, city):
      - If not configured: returns an explicit "AI summary unavailable: missing Google Gemini API key." message.
      - Builds a compact prompt including timezone and location. JSON serialization uses Jackson. On serialization failure, falls back to a minimal safe JSON via safeReport().
      - Uses a lazily initialized ChatLanguageModel with double-checked locking and a volatile cachedModel field.
      - Any RuntimeException from model.generate() returns a controlled fallback: "AI summary unavailable at the moment. Reason: ..." to avoid failing the request.
- Controller/API behavior
  - /api/ai-summary: Accepts WeatherReport JSON in body, optional timezone and city parameters. Response includes fields summary (String), model ("gemini"), configured (boolean). Integration tests assert these.
  - /api/cities/search: Validates parameters; limit parameter has defaults and caps (see tests for behavior and edge cases).
  - /report: Validates mandatory lat/lon; passes city and values to the Thymeleaf template.
- Code style and testing style
  - Prefer Spring Boot test slices (@WebMvcTest) for controller-only units; use @SpringBootTest + MockMvc for integrated flows.
  - Mock external integrations using @TestConfiguration with @Primary bean overrides. Prefer Mockito stubs for clarity, as shown in EndToEndIntegrationTest and AiSummaryControllerIntegrationTest.
  - Keep tests deterministic; avoid network calls in tests. City data is static.
  - use final var instead of type where possible for local variables
  - prefer wrapper types instead of primitives
  - use List.of instead of Arrays.asList
- Troubleshooting
  - Tests fail with missing AI key: Ensure ActiveProfiles("test") and application-test.properties is on the classpath. Alternatively, set -Dspring.profiles.active=test or define ai.gemini.api-key.
  - Unsupported Java version: Ensure local JDK 21; Gradle toolchain should provision, but corporate networks can block downloads—preinstall JDK 21 in that case.
  - Port conflicts when running bootRun: Set -Dserver.port=0 or another port.
  - Slow tests due to full context: Consider @WebMvcTest slices and mock only required beans.
  - Flaky AI tests: Do not rely on external AI responses; always stub summarize().

4. Commands Quick Reference
- Build: ./gradlew build
- Run app: ./gradlew bootRun
- Run all tests: ./gradlew test
- Run one class: ./gradlew test --tests "com.example.weatherapp.web.ReportControllerIntegrationTest"
- Run one method: ./gradlew test --tests "com.example.weatherapp.web.ReportControllerIntegrationTest.methodName"
- With profile override: ./gradlew test -Dspring.profiles.active=test

Notes
- The examples above have been validated against current project structure and dependencies. For demonstration tests, create them locally and delete before committing, as per repository policy.
