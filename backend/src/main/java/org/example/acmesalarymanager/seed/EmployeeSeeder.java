package org.example.acmesalarymanager.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.acmesalarymanager.currency.CountryCatalog;
import org.example.acmesalarymanager.currency.CurrencyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeSeeder implements CommandLineRunner {

    private static final int EMPLOYEE_COUNT = 10_000;
    private static final int BATCH_SIZE = 500;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CountryCatalog countryCatalog;
    private final CurrencyConverter currencyConverter;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        Long existing = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Map.of(), Long.class);
        if (existing != null && existing > 0) {
            log.info("Employees table already has {} rows, skipping seed", existing);
            return;
        }

        log.info("Seeding {} employees...", EMPLOYEE_COUNT);
        long start = System.currentTimeMillis();

        EmployeeDataGenerator generator = new EmployeeDataGenerator(countryCatalog, currencyConverter);
        List<SeedEmployee> employees = generator.generate(EMPLOYEE_COUNT);
        insertInBatches(employees);

        log.info("Seeded {} employees in {} ms", EMPLOYEE_COUNT, System.currentTimeMillis() - start);
    }

    private void insertInBatches(List<SeedEmployee> employees) {
        String sql = """
                INSERT INTO employees
                    (full_name, email, job_title, department, country, currency,
                     salary, salary_usd, hire_date, status, created_at, updated_at)
                VALUES
                    (:fullName, :email, :jobTitle, :department, :country, :currency,
                     :salary, :salaryUsd, :hireDate, :status, :now, :now)
                """;

        LocalDateTime now = LocalDateTime.now();
        MapSqlParameterSource[] batch = employees.stream()
                .map(e -> new MapSqlParameterSource()
                        .addValue("fullName", e.fullName())
                        .addValue("email", e.email())
                        .addValue("jobTitle", e.jobTitle())
                        .addValue("department", e.department())
                        .addValue("country", e.country())
                        .addValue("currency", e.currency())
                        .addValue("salary", e.salaryLocal(), Types.DECIMAL)
                        .addValue("salaryUsd", e.salaryUsd(), Types.DECIMAL)
                        .addValue("hireDate", e.hireDate())
                        .addValue("status", e.status())
                        .addValue("now", now))
                .toArray(MapSqlParameterSource[]::new);

        for (int i = 0; i < batch.length; i += BATCH_SIZE) {
            MapSqlParameterSource[] chunk = java.util.Arrays.copyOfRange(
                    batch, i, Math.min(i + BATCH_SIZE, batch.length));
            jdbcTemplate.batchUpdate(sql, chunk);
        }
    }
}