package org.example.acmesalarymanager.currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyConverterRoundTripTest {

    private final CurrencyConverter converter = new CurrencyConverter();

    @Test
    void fromUsdIsTheInverseOfToUsd() {
        BigDecimal usdTarget = new BigDecimal("100000");

        for (CountryCurrency entry : new CountryCatalog().all()) {
            BigDecimal local = converter.fromUsd(usdTarget, entry.currency());
            BigDecimal backToUsd = converter.toUsd(local, entry.currency());

            assertThat(backToUsd).isCloseTo(usdTarget, org.assertj.core.data.Offset.offset(new BigDecimal("1")));
        }
    }
}