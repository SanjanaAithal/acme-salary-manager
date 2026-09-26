package org.example.acmesalarymanager.seed;

import org.example.acmesalarymanager.currency.CountryCatalog;
import org.example.acmesalarymanager.currency.CurrencyConverter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeDataGeneratorTest {

    private final CountryCatalog countryCatalog = new CountryCatalog();
    private final CurrencyConverter currencyConverter = new CurrencyConverter();

    @Test
    void sameSeedProducesIdenticalData() {
        List<SeedEmployee> first = generator().generate(200);
        List<SeedEmployee> second = generator().generate(200);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void generatesTheRequestedCount() {
        assertThat(generator().generate(500)).hasSize(500);
    }

    @Test
    void everyEmailIsUnique() {
        List<SeedEmployee> employees = generator().generate(2000);

        Set<String> emails = employees.stream().map(SeedEmployee::email).collect(Collectors.toSet());
        assertThat(emails).hasSize(employees.size());
    }

    @Test
    void everyEmployeeHasPositiveSalaryInBothCurrencies() {
        for (SeedEmployee employee : generator().generate(500)) {
            assertThat(employee.salaryLocal()).isGreaterThan(BigDecimal.ZERO);
            assertThat(employee.salaryUsd()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Test
    void countryAndCurrencyAlwaysMatchTheCatalog() {
        for (SeedEmployee employee : generator().generate(500)) {
            assertThat(employee.currency())
                    .isEqualTo(countryCatalog.require(employee.country()).currency());
        }
    }

    @Test
    void hireDatesFallWithinTheExpectedRange() {
        for (SeedEmployee employee : generator().generate(500)) {
            assertThat(employee.hireDate()).isBetween(LocalDate.of(2015, 1, 1), LocalDate.of(2025, 1, 1));
        }
    }

    @Test
    void statusIsAlwaysAKnownValue() {
        for (SeedEmployee employee : generator().generate(500)) {
            assertThat(employee.status()).isIn("ACTIVE", "ON_LEAVE", "TERMINATED");
        }
    }

    @Test
    void managerRolesAreOnAveragePaidMoreThanNonManagerRoles() {
        List<SeedEmployee> employees = generator().generate(3000);

        double managerAvg = employees.stream()
                .filter(e -> e.jobTitle().toLowerCase().contains("manager"))
                .mapToDouble(e -> e.salaryUsd().doubleValue())
                .average().orElseThrow();
        double otherAvg = employees.stream()
                .filter(e -> !e.jobTitle().toLowerCase().contains("manager"))
                .mapToDouble(e -> e.salaryUsd().doubleValue())
                .average().orElseThrow();

        assertThat(managerAvg).isGreaterThan(otherAvg);
    }

    private EmployeeDataGenerator generator() {
        return new EmployeeDataGenerator(countryCatalog, currencyConverter);
    }
}