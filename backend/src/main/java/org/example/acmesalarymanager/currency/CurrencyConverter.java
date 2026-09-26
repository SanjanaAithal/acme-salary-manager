package org.example.acmesalarymanager.currency;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

@Component
public class CurrencyConverter {

    public static final LocalDate RATES_AS_OF = LocalDate.of(2025, 1, 1);

    private static final Map<String, BigDecimal> USD_PER_UNIT = Map.of(
            "USD", new BigDecimal("1"),
            "EUR", new BigDecimal("1.04"),
            "GBP", new BigDecimal("1.25"),
            "INR", new BigDecimal("0.0117"),
            "CAD", new BigDecimal("0.70"),
            "AUD", new BigDecimal("0.62"),
            "SGD", new BigDecimal("0.74"),
            "JPY", new BigDecimal("0.0064"));

    public BigDecimal toUsd(BigDecimal amount, String currency) {
        return amount.multiply(rateFor(currency)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal fromUsd(BigDecimal usdAmount, String currency) {
        return usdAmount.divide(rateFor(currency), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal rateFor(String currency) {
        BigDecimal rate = USD_PER_UNIT.get(currency);
        if (rate == null) {
            throw new IllegalArgumentException("No exchange rate for currency " + currency);
        }
        return rate;
    }
}