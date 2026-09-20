# ACME Salary Manager: Design Notes

Written before the code, so I can build against a plan and update it as I learn.

## Approach

Build the backend first, test-first, because it defines the API the UI depends on. Then build the Angular UI against the finished API. Each step is a small commit.

## Architecture

A single Spring Boot application with a layered structure, packaged by feature:

```
com.acme.salary
├── employee     controller, service, repository, entity, dto
├── analytics    controller, service, response models
├── common       error handling, config
└── seed         data seeder
```

- **Controller:** HTTP only. Reads input, calls a service, returns a response. No business logic.
- **Service:** business rules and transactions. This is where most unit tests live.
- **Repository:** Spring Data JPA, plus a few custom queries for analytics.
- **DTOs:** the API never exposes entities directly, so the database can change without breaking the API.
- **Errors:** one `@RestControllerAdvice` returns a consistent error body for validation failures, not found, and duplicate emails.

## Data model

One main table, `employees`:

| Column | Type | Notes |
|---|---|---|
| id | bigint | generated |
| full_name | varchar | required |
| email | varchar | required, unique |
| job_title | varchar | required, indexed |
| department | varchar | required, indexed |
| country | varchar | required, indexed |
| salary | decimal(15,2) | required, > 0, `BigDecimal` in Java |
| hire_date | date | required, not in the future |
| status | varchar | ACTIVE, ON_LEAVE, TERMINATED |
| created_at, updated_at | timestamp | set by the app |

Schema is managed with Flyway migrations, and Hibernate only validates it (`ddl-auto: validate`), so the schema is explicit and reviewable.

## API (draft)

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/employees` | list with `search`, `country`, `department`, `jobTitle`, `status`, `page`, `size`, `sort` |
| GET | `/api/employees/{id}` | one employee |
| POST | `/api/employees` | create |
| PUT | `/api/employees/{id}` | update |
| DELETE | `/api/employees/{id}` | delete |
| GET | `/api/employees/filter-options` | distinct countries, departments, job titles for the filter dropdowns |
| GET | `/api/analytics/summary` | headcount, total payroll, average salary |
| GET | `/api/analytics/salary-stats?groupBy=` | count, min, max, average, median by country, department or job title |
| GET | `/api/analytics/salary-distribution` | salary bands with counts |

## Key decisions and trade-offs

- **`BigDecimal` for salary.** Floating point can't represent money exactly.
- **Aggregation in SQL.** Analytics use `GROUP BY` in the database instead of loading 10,000 rows into memory. It's faster, and it scales better.
- **Indexes on filter columns.** Country, department, job title and status are what HR filters by.
- **Server-side paging.** The UI only ever receives one page.
- **Median.** Not every SQL database has a portable median function. H2 has one, and I'll note that it's tied to the database if I keep it in a native query.
- **H2 in file mode.** Zero setup, and it works smoothly with Hibernate. The code uses standard JPA, so moving to Postgres later is a small change.
- **Deterministic seed.** A fixed random seed and JDBC batch inserts, so 10,000 rows load in about a second and are identical on every run.

## Open questions (sent to the hiring team)

- **Currency:** employees are in several countries. Grouping salaries by department or job title across countries only makes sense if they're in a comparable currency. I have deliberately not added a currency column yet. Once I know the expected handling, I'll add it in a new migration.

## Testing approach

- **Service unit tests:** JUnit 5, Mockito and AssertJ. No Spring context, so they run in milliseconds.
- **Repository tests:** `@DataJpaTest` on embedded H2, mainly for filters and analytics queries.
- **API tests:** `@WebMvcTest` with MockMvc for status codes, validation errors and JSON shape.
- **Determinism:** no random data in tests, and a fixed clock where dates matter.

## Performance targets

- Employee list under 300 ms with filters on 10,000 rows.
- Analytics endpoints under 500 ms.
- I'll check both with the seeded data before calling it done.