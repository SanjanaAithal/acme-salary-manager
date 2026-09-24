package org.example.acmesalarymanager.currency;

public class UnsupportedCountryException extends RuntimeException {

    public UnsupportedCountryException(String country) {
        super("Country '" + country + "' is not supported");
    }
}