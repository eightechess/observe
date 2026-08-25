package com.mydemo.observe.model;

import java.math.BigDecimal;

public record DemoResponse(Weather weather, Currency currency) {

    public record Weather(
            double latitude,
            double longitude,
            String timezone,
            String observationTime,
            double temperature,
            String temperatureUnit,
            double apparentTemperature,
            int weatherCode,
            double windSpeed,
            String windSpeedUnit) {
    }

    public record Currency(
            BigDecimal amount,
            String from,
            String to,
            BigDecimal rate,
            BigDecimal convertedAmount,
            String rateDate) {
    }
}
