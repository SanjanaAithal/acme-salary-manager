# ACME Salary Manager: Requirements

## Goal

ACME's HR team manages salaries for 10,000 employees across several countries in Excel. It's slow and error-prone. I'm building a web app where the HR Manager can keep salary records accurate and see how the company pays people.

## Questions the HR Manager can answer

The brief gave no fixed reports, so I defined these:

1. What do we spend on salaries in total, and per country and department?
2. What's the typical pay (median, average) per job title, department and country?
3. How wide is the pay range within a role?
4. How is pay spread across the company (salary bands)?
5. Who are the highest and lowest paid, overall or within a group?
6. How does pay for the same job title differ between countries?

## In scope

- **Employees:** add, view, edit, delete. Fields: name, email, job title, department, country, salary, hire date, status.
- **List:** server-side paging and sorting, search by name or email, filters for country, department, job title and status.
- **Validation:** required fields, positive salary, valid and unique email, supported country.
- **Insights:** summary cards, stats (headcount, min, max, average, median) by country, department and job title, a salary distribution chart, and top and bottom paid lists.
- **Delivery:** 10,000-employee seed script, tests, Swagger docs, deployed app, demo video.
- **If time permits:** CSV export, salary change history, outlier view.

## Salaries across countries

The hiring team left this open, so here is my decision:

- **Local currency is the source of truth.** It's what HR maintains and payroll pays.
- **Country decides currency**, from a fixed table. HR picks the country, the currency follows.
- **A USD equivalent is stored on save**, using a static, dated exchange rate table. All comparisons and analytics use USD. The list shows both.
- **Why:** HR needs exact local amounts, and org-wide questions need one currency. Static rates keep results deterministic and testable. Storing USD keeps analytics as simple SQL.
- **Limit:** rates are approximate, and changing a rate doesn't update stored values.

## Left out, and why

- **Live or historical exchange rates:** external dependency, and results would change over time.
- **Currency independent of country, multiple reporting currencies:** more edge cases, no gain for the questions above.
- **Login and roles:** one user type. Next step would be Spring Security with SSO.
- **Payroll, tax, payslips:** a different problem with country-specific rules.
- **Bonuses, equity, benefits, reviews:** base salary only.
- **Approvals, notifications, org chart:** not needed for salary management.
- **Multi-tenancy, other languages:** one company, English only.
- **Excel import screen:** one-time job, better done as a script.

## Assumptions and success criteria

- Salary is annual base pay. One HR user at a time, last save wins.
- Employee list under 300 ms, analytics under 500 ms on 10,000 rows, calculated in SQL.
- Salary stored as `BigDecimal`. Tests are fast and deterministic.

## Tech

Java 17, Spring Boot, JPA, Flyway, H2 (file mode), Angular with Angular Material.