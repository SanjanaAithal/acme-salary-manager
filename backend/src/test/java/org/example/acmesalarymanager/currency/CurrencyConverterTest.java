package org.example.acmesalarymanager.currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyConverterTest {

    private final CurrencyConverter converter = new CurrencyConverter();

    @Test
    void usdStaysTheSame() {
        assertThat(converter.toUsd(new BigDecimal("1234.56"), "USD")).isEqualByComparingTo("1234.56");
    }

    @Test
    void convertsUsingTheStaticRate() {
        assertThat(converter.toUsd(new BigDecimal("85000.00"), "INR")).isEqualByComparingTo("994.50");
        assertThat(converter.toUsd(new BigDecimal("1000.00"), "EUR")).isEqualByComparingTo("1040.00");
    }

    @Test
    void roundsHalfUpToTwoDecimals() {
        // 0.10 * 1.25 = 0.125, which half-up rounds to 0.13 (half-even would give 0.12)
        assertThat(converter.toUsd(new BigDecimal("0.10"), "GBP")).isEqualByComparingTo("0.13");
    }

    @Test
    void alwaysReturnsTwoDecimalPlaces() {
        assertThat(converter.toUsd(new BigDecimal("5000000"), "JPY").scale()).isEqualTo(2);
    }

    @Test
    void rejectsCurrencyWithoutRate() {
        assertThatThrownBy(() -> converter.toUsd(BigDecimal.TEN, "XYZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void everySupportedCountryHasAConvertibleCurrency() {
        for (CountryCurrency entry : new CountryCatalog().all()) {
            assertThatCode(() -> converter.toUsd(BigDecimal.ONE, entry.currency()))
                    .as("rate for %s", entry.currency())
                    .doesNotThrowAnyException();
        }
    }
}