package com.mydemo.observe.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherResponse(
        double latitude,
        double longitude,
        String timezone,
        Current current,
        @JsonProperty("current_units") CurrentUnits currentUnits) {

    public record Current(
            String time,
            @JsonProperty("temperature_2m") double temperature,
            @JsonProperty("apparent_temperature") double apparentTemperature,
            @JsonProperty("weather_code") int weatherCode,
            @JsonProperty("wind_speed_10m") double windSpeed) {
    }

    public record CurrentUnits(
            @JsonProperty("temperature_2m") String temperature,
            @JsonProperty("wind_speed_10m") String windSpeed) {
    }
}
