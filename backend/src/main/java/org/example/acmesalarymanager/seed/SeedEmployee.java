package org.example.acmesalarymanager.seed;

import java.math.BigDecimal;
import java.time.LocalDate;


public record SeedEmployee(
        String fullName,
        String email,
        String jobTitle,
        String department,
        String country,
        String currency,
        BigDecimal salaryLocal,
        BigDecimal salaryUsd,
        LocalDate hireDate,
        String status
) {
}