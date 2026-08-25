package com.mydemo.observe.model;

import java.math.BigDecimal;

public record CurrencyRateResponse(
        String date,
        String base,
        String quote,
        BigDecimal rate) {
}
