# Weather App Java

![CI Status](https://github.com/isaric/weather-app-java/actions/workflows/ci.yml/badge.svg?branch=main)

A web application for providing a weather report along with an AI summary with a Java backend.

## Features

- **Weather Reports**: Get detailed weather forecasts for any location using coordinates
- **City Search**: Search for cities to easily find coordinates
- **AI-Powered Summaries**: Generate human-readable weather summaries using Google's Gemini AI
- **Responsive Web Interface**: User-friendly interface built with Thymeleaf templates

## Technologies Used

- **Java 21**: Modern Java language features
- **Spring Boot 3.5.5**: Framework for building web applications
- **Thymeleaf**: Server-side Java template engine
- **LangChain4j**: Java library for working with LLMs
- **Google Gemini AI**: AI model for generating weather summaries
- **Jackson**: JSON processing library
- **Open-Meteo API**: Weather data provider (client-side integration)
- **Gradle**: Build and dependency management

## Setup and Installation

### Prerequisites

- Java 21 or higher
- Gradle (or use the included Gradle wrapper)
- Google Gemini API key (for AI summaries)

### Configuration

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/weather-app-java.git
   cd weather-app-java
   ```

2. Configure the Google Gemini API key:
   - Option 1: Set environment variable:
     ```bash
     export AI_API_KEY=your_gemini_api_key
     ```
   - Option 2: Add to application.properties:
     ```properties
     ai.gemini.api-key=your_gemini_api_key
     ```

3. (Optional) Configure the Gemini model:
   ```bash
   export GOOGLE_GEMINI_MODEL=gemini-1.5-flash
   ```

### Running the Application

#### Option 1: Using Gradle wrapper
```bash
./gradlew bootRun
```

#### Option 2: Using Docker
1. Build the Docker image:
```bash
docker build -t weather-app-java .
```

2. Run the container:
```bash
docker run -p 8080:8080 -e AI_API_KEY=your_gemini_api_key weather-app-java
```

The application will be available at http://localhost:8080

## Usage

1. Open the application in your browser at http://localhost:8080
2. Search for a city using the search box
3. View the weather report for the selected location
4. Read the AI-generated summary for practical insights and activity suggestions

## API Endpoints

### Web Pages
- `GET /`: Home page
- `GET /report`: Weather report page (requires lat, lon parameters)

### REST APIs
- `GET /api/cities/search`: Search for cities
  - Parameters:
    - `q`: Search query
    - `limit`: Maximum number of results (default: 10, max: 50)
- `POST /api/ai-summary`: Generate AI summary for weather data
  - Request body: WeatherReport JSON
  - Parameters:
    - `timezone`: Optional timezone
    - `city`: Optional city name

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
