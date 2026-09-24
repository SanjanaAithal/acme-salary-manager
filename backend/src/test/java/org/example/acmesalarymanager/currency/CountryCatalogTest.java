package org.example.acmesalarymanager.currency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CountryCatalogTest {

    private final CountryCatalog catalog = new CountryCatalog();

    @Test
    void resolvesCountryIgnoringCaseAndSpacesAndReturnsCanonicalName() {
        CountryCurrency result = catalog.require("  india ");

        assertThat(result.country()).isEqualTo("India");
        assertThat(result.currency()).isEqualTo("INR");
    }

    @Test
    void euroCountriesShareTheSameCurrency() {
        assertThat(catalog.require("Germany").currency()).isEqualTo("EUR");
        assertThat(catalog.require("France").currency()).isEqualTo("EUR");
        assertThat(catalog.require("Spain").currency()).isEqualTo("EUR");
    }

    @Test
    void rejectsUnsupportedCountry() {
        assertThatThrownBy(() -> catalog.require("Atlantis"))
                .isInstanceOf(UnsupportedCountryException.class)
                .hasMessageContaining("Atlantis");
    }

    @Test
    void rejectsMissingCountry() {
        assertThatThrownBy(() -> catalog.require(null))
                .isInstanceOf(UnsupportedCountryException.class);
    }

    @Test
    void listsSupportedCountriesAlphabetically() {
        assertThat(catalog.all())
                .extracting(CountryCurrency::country)
                .hasSize(10)
                .isSorted();
    }
}