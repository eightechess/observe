package com.mydemo.observe.service;

import com.mydemo.observe.model.WeatherResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class WeatherClient {

    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";

    private final RestTemplate restTemplate;

    public WeatherClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WeatherResponse getCurrentWeather(double latitude, double longitude) {
        URI uri = UriComponentsBuilder.fromUriString(WEATHER_URL)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("current", "temperature_2m,apparent_temperature,weather_code,wind_speed_10m")
                .queryParam("temperature_unit", "fahrenheit")
                .queryParam("wind_speed_unit", "mph")
                .queryParam("timezone", "auto")
                .build()
                .encode()
                .toUri();

        return restTemplate.getForObject(uri, WeatherResponse.class);
    }
}
