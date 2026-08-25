package com.mydemo.observe.service;

import com.mydemo.observe.model.CurrencyRateResponse;
import com.mydemo.observe.model.DemoResponse;
import com.mydemo.observe.model.WeatherResponse;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ObservabilityDemoService {

    private final WeatherClient weatherClient;
    private final CurrencyClient currencyClient;
    private final Tracer tracer;

    public ObservabilityDemoService(
            WeatherClient weatherClient,
            CurrencyClient currencyClient,
            Tracer tracer) {
        this.weatherClient = weatherClient;
        this.currencyClient = currencyClient;
        this.tracer = tracer;
    }

    public DemoResponse run(
            double latitude,
            double longitude,
            BigDecimal amount,
            String from,
            String to) {

        Span workflowSpan = tracer.spanBuilder("observability-demo")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope ignored = workflowSpan.makeCurrent()) {
            workflowSpan.setAttribute("demo.currency.from", from);
            workflowSpan.setAttribute("demo.currency.to", to);

            WeatherResponse weather = weatherClient.getCurrentWeather(latitude, longitude);
            CurrencyRateResponse currencyRate = currencyClient.getRate(from, to);

            BigDecimal convertedAmount = amount
                    .multiply(currencyRate.rate())
                    .setScale(2, RoundingMode.HALF_UP);

            DemoResponse.Weather weatherResult = new DemoResponse.Weather(
                    weather.latitude(),
                    weather.longitude(),
                    weather.timezone(),
                    weather.current().time(),
                    weather.current().temperature(),
                    weather.currentUnits().temperature(),
                    weather.current().apparentTemperature(),
                    weather.current().weatherCode(),
                    weather.current().windSpeed(),
                    weather.currentUnits().windSpeed());

            DemoResponse.Currency currencyResult = new DemoResponse.Currency(
                    amount,
                    currencyRate.base(),
                    currencyRate.quote(),
                    currencyRate.rate(),
                    convertedAmount,
                    currencyRate.date());

            return new DemoResponse(weatherResult, currencyResult);
        } catch (RuntimeException ex) {
            workflowSpan.recordException(ex);
            workflowSpan.setStatus(StatusCode.ERROR);
            throw ex;
        } finally {
            workflowSpan.end();
        }
    }
}
