package com.mydemo.observe.controller;

import com.mydemo.observe.model.DemoResponse;
import com.mydemo.observe.service.ObservabilityDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/demo")
public class ObservabilityDemoController {

    private final ObservabilityDemoService demoService;

    public ObservabilityDemoController(
            ObservabilityDemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping
    public ResponseEntity<DemoResponse> runDemo(

            @RequestParam(
                    name = "latitude",
                    defaultValue = "32.7357")
            double latitude,

            @RequestParam(
                    name = "longitude",
                    defaultValue = "-97.1081")
            double longitude,

            @RequestParam(
                    name = "amount",
                    defaultValue = "100.00")
            BigDecimal amount,

            @RequestParam(
                    name = "from",
                    defaultValue = "USD")
            String from,

            @RequestParam(
                    name = "to",
                    defaultValue = "EUR")
            String to) {

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "amount must be greater than zero"
            );
        }

        DemoResponse response = demoService.run(
                latitude,
                longitude,
                amount,
                from.toUpperCase(),
                to.toUpperCase()
        );

        return ResponseEntity.ok(response);
    }
}