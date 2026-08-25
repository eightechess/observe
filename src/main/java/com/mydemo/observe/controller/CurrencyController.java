package com.mydemo.observe.controller;

import com.mydemo.observe.model.CurrencyRateResponse;
import com.mydemo.observe.service.CurrencyClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyClient currencyClient;

    public CurrencyController(CurrencyClient currencyClient) {
        this.currencyClient = currencyClient;
    }

    @GetMapping
    public ResponseEntity<CurrencyRateResponse> getRate(
            @RequestParam(defaultValue = "USD") String from,
            @RequestParam(defaultValue = "EUR") String to) {

        return ResponseEntity.ok(currencyClient.getRate(from, to));
    }
}
