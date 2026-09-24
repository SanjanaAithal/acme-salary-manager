package org.example.acmesalarymanager.currency;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CountryCatalog {

    private static final List<CountryCurrency> SUPPORTED = List.of(
            new CountryCurrency("Australia", "AUD"),
            new CountryCurrency("Canada", "CAD"),
            new CountryCurrency("France", "EUR"),
            new CountryCurrency("Germany", "EUR"),
            new CountryCurrency("India", "INR"),
            new CountryCurrency("Japan", "JPY"),
            new CountryCurrency("Singapore", "SGD"),
            new CountryCurrency("Spain", "EUR"),
            new CountryCurrency("United Kingdom", "GBP"),
            new CountryCurrency("United States", "USD"));

    private final Map<String, CountryCurrency> byKey = SUPPORTED.stream()
            .collect(Collectors.toMap(entry -> key(entry.country()), Function.identity()));

    public List<CountryCurrency> all() {
        return SUPPORTED;
    }

    public CountryCurrency require(String country) {
        CountryCurrency found = country == null ? null : byKey.get(key(country));
        if (found == null) {
            throw new UnsupportedCountryException(country);
        }
        return found;
    }

    private static String key(String country) {
        return country.trim().toLowerCase(Locale.ROOT);
    }
}