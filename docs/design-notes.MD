# ACME Salary Manager: Design Notes

## Approach

Backend first and test-first, since it defines the API. Then the Angular UI on top. Small, incremental commits.

## Architecture

One Spring Boot app, layered and grouped by feature:

```
org.example.acmesalarymanager
├── employee    controller, service, repository, entity, dto
├── analytics   controller, service, response models
├── currency    country-to-currency table, static rates, converter
├── common      error handling, config
└── seed        data seeder
```

Controllers handle HTTP only. Services hold the rules. DTOs keep entities out of the API. One `@RestControllerAdvice` returns consistent errors.

## Data model

Table `employees`: id, full_name, email (unique), job_title, department, country, salary (local), currency, salary_usd, hire_date, status, created_at, updated_at.

Flyway manages the schema, and Hibernate only validates it.

## API (draft)

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/employees` | list with search, filters, paging, sorting |
| GET, POST, PUT, DELETE | `/api/employees/{id}` | single employee (POST on the collection) |
| GET | `/api/employees/filter-options` | dropdown values |
| GET | `/api/analytics/summary` | headcount, total payroll, average |
| GET | `/api/analytics/salary-stats?groupBy=` | count, min, max, average, median in USD |
| GET | `/api/analytics/salary-distribution` | salary bands |

## Key decisions

- **`BigDecimal` for money.** No floating point.
- **Local salary plus stored USD.** Rates are static and dated, and conversion rounds half-up to two decimals. Analytics stay plain SQL.
- **Aggregation in SQL** with `GROUP BY`, plus indexes on the filter columns, added after measuring.
- **Server-side paging** with `id` as a sort tie-breaker, and whitelisted sort fields.
- **H2 in file mode.** Standard JPA, so moving to Postgres later is small.
- **Deterministic seed** with a fixed random seed and batch inserts.

## Testing

Service tests use Mockito with no Spring context. Repository tests use `@DataJpaTest`. API tests use `@WebMvcTest`. A fixed clock keeps tests deterministic.

## Performance targets

List under 300 ms and analytics under 500 ms on 10,000 rows, checked with the seeded data.