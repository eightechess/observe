package com.mydemo.observe.controller;

import com.mydemo.observe.model.WeatherResponse;
import com.mydemo.observe.service.WeatherClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherClient weatherClient;

    public WeatherController(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @GetMapping
    public ResponseEntity<WeatherResponse> getCurrentWeather(
            @RequestParam(defaultValue = "32.7357") double latitude,
            @RequestParam(defaultValue = "-97.1081") double longitude) {

        return ResponseEntity.ok(
                weatherClient.getCurrentWeather(latitude, longitude));
    }
}
