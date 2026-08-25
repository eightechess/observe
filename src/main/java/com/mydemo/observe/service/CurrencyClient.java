package com.mydemo.observe.service;

import com.mydemo.observe.model.CurrencyRateResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CurrencyClient {

    private static final String RATE_URL =
            "https://api.frankfurter.dev/v2/rate/{base}/{quote}";

    private final RestTemplate restTemplate;

    public CurrencyClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CurrencyRateResponse getRate(String from, String to) {
        return restTemplate.getForObject(
                RATE_URL,
                CurrencyRateResponse.class,
                from.toUpperCase(),
                to.toUpperCase());
    }
}
