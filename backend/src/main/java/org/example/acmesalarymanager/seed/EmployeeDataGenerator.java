package org.example.acmesalarymanager.seed;

import org.example.acmesalarymanager.currency.CountryCatalog;
import org.example.acmesalarymanager.currency.CountryCurrency;
import org.example.acmesalarymanager.currency.CurrencyConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class EmployeeDataGenerator {

    public static final long DEFAULT_SEED = 42L;

    private static final String[] FIRST_NAMES = {
            "Asha", "Ravi", "John", "Maria", "Ashley", "Wei", "Yuki", "Liam", "Emma", "Noah",
            "Olivia", "Arjun", "Priya", "Sofia", "Lucas", "Mia", "Ethan", "Ana", "Hiro", "Ken",
            "Grace", "Ivy", "Omar", "Layla", "Raj", "Neha", "Tom", "Sara", "Ben", "Zoe",
            "Kai", "Nina", "Leo", "Amara", "Diego", "Elena", "Felix", "Hana", "Ivan", "Jade"
    };

    private static final String[] LAST_NAMES = {
            "Rao", "Kumar", "Smith", "Garcia", "Brown", "Chen", "Tanaka", "Murphy", "Silva", "Khan",
            "Nakamura", "Patel", "Singh", "Martinez", "Wilson", "Lopez", "Kim", "Nguyen", "Dubois", "Rossi",
            "Schmidt", "Müller", "Fischer", "Andersen", "Larsen", "Clarke", "Bennett", "Cole", "Reed", "Hughes"
    };

    /** Job titles per department, roughly junior to senior. The order also sets the salary band. */
    private static final List<DepartmentRoles> DEPARTMENTS = List.of(
            new DepartmentRoles("Engineering", List.of(
                    "Software Engineer", "Senior Software Engineer", "QA Engineer", "Engineering Manager")),
            new DepartmentRoles("Sales", List.of(
                    "Sales Executive", "Account Executive", "Sales Manager")),
            new DepartmentRoles("Finance", List.of(
                    "Financial Analyst", "Accountant", "Finance Manager")),
            new DepartmentRoles("Human Resources", List.of(
                    "HR Executive", "HR Business Partner", "HR Manager")),
            new DepartmentRoles("Marketing", List.of(
                    "Marketing Executive", "Marketing Manager")),
            new DepartmentRoles("Operations", List.of(
                    "Operations Analyst", "Operations Manager"))
    );

    /** Annual base salary range in USD for each seniority band, independent of country. */
    private static final Band JUNIOR = new Band(45_000, 75_000);
    private static final Band SENIOR = new Band(75_000, 110_000);
    private static final Band MANAGER = new Band(110_000, 160_000);

    private static final LocalDate HIRE_RANGE_START = LocalDate.of(2015, 1, 1);
    private static final LocalDate HIRE_RANGE_END = LocalDate.of(2025, 1, 1);

    /** Roughly realistic: most employees are active, a few on leave, fewer terminated. */
    private static final String[] STATUS_POOL = {
            "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE", "ACTIVE",
            "ACTIVE", "ACTIVE", "ACTIVE", "ON_LEAVE", "TERMINATED"
    };

    private final CountryCatalog countryCatalog;
    private final CurrencyConverter currencyConverter;
    private final long seed;

    public EmployeeDataGenerator(CountryCatalog countryCatalog, CurrencyConverter currencyConverter) {
        this(countryCatalog, currencyConverter, DEFAULT_SEED);
    }

    public EmployeeDataGenerator(CountryCatalog countryCatalog, CurrencyConverter currencyConverter, long seed) {
        this.countryCatalog = countryCatalog;
        this.currencyConverter = currencyConverter;
        this.seed = seed;
    }

    public List<SeedEmployee> generate(int count) {
        Random random = new Random(seed);
        List<CountryCurrency> countries = countryCatalog.all();
        Set<String> usedEmails = new HashSet<>();
        List<SeedEmployee> employees = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String firstName = pick(random, FIRST_NAMES);
            String lastName = pick(random, LAST_NAMES);
            String fullName = firstName + " " + lastName;
            String email = uniqueEmail(firstName, lastName, usedEmails);

            DepartmentRoles department = pick(random, DEPARTMENTS.toArray(new DepartmentRoles[0]));
            int roleIndex = random.nextInt(department.jobTitles().size());
            String jobTitle = department.jobTitles().get(roleIndex);
            Band band = bandFor(jobTitle);

            CountryCurrency country = pick(random, countries.toArray(new CountryCurrency[0]));
            BigDecimal salaryUsd = BigDecimal.valueOf(band.randomWithin(random));
            BigDecimal salaryLocal = currencyConverter.fromUsd(salaryUsd, country.currency());

            LocalDate hireDate = randomDate(random, HIRE_RANGE_START, HIRE_RANGE_END);
            String status = pick(random, STATUS_POOL);

            employees.add(new SeedEmployee(
                    fullName, email, jobTitle, department.name(),
                    country.country(), country.currency(),
                    salaryLocal, salaryUsd, hireDate, status));
        }

        return employees;
    }

    private static Band bandFor(String jobTitle) {
        String lower = jobTitle.toLowerCase(Locale.ROOT);
        if (lower.contains("manager")) {
            return MANAGER;
        }
        if (lower.contains("senior") || lower.contains("business partner")) {
            return SENIOR;
        }
        return JUNIOR;
    }

    private static String uniqueEmail(String firstName, String lastName, Set<String> used) {
        String base = (firstName + "." + lastName).toLowerCase(Locale.ROOT).replace(" ", "");
        String email = base + "@acme.com";
        int suffix = 1;
        while (!used.add(email)) {
            email = base + suffix + "@acme.com";
            suffix++;
        }
        return email;
    }

    private static LocalDate randomDate(Random random, LocalDate start, LocalDate end) {
        long startEpoch = start.toEpochDay();
        long endEpoch = end.toEpochDay();
        long randomEpoch = startEpoch + (long) (random.nextDouble() * (endEpoch - startEpoch));
        return LocalDate.ofEpochDay(randomEpoch);
    }

    private static <T> T pick(Random random, T[] values) {
        return values[random.nextInt(values.length)];
    }

    private record DepartmentRoles(String name, List<String> jobTitles) {
    }

    private record Band(int minUsd, int maxUsd) {
        int randomWithin(Random random) {
            return minUsd + random.nextInt(maxUsd - minUsd);
        }
    }
}