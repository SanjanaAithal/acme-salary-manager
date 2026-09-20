# ACME Salary Manager: Requirements

## Goal

ACME's HR team keeps salary data for 10,000 employees across several countries in Excel files. It's slow to update, easy to get wrong, and hard to answer questions from. I'm building a web app where the HR Manager can keep salary records accurate and quickly see how the company pays people, without opening a spreadsheet.

## Who it's for

The HR Manager. There's just one user type, so I'm designing everything around their day-to-day work.

The questions I want the app to answer fast:

- What's the average, median, lowest and highest salary in each country, department and job title?
- How is pay spread across the company?
- How many people do we have, and what's our total payroll?
- Who are the top and bottom paid in a given role or country?
- Who is paid far above or below others in the same role?

## What I'm building

**Managing employees**
- Add, view, edit and delete employees. Each has a name, email, job title, department, country, salary, hire date and status.
- A list that handles 10,000 rows well: paging and sorting done on the server, search by name or email, and filters for country, department, job title and status.
- Basic checks so bad data can't get in: required fields, a positive salary, and a valid, unique email.

**Insights**
- Summary numbers: headcount, total payroll and average salary.
- Headcount and min, max, average and median salary grouped by country, department and job title.
- A salary distribution chart.

**Also part of the deliverable**
- A seed script that creates 10,000 employees, with the same data every run.
- Fast, reliable tests.
- API docs (Swagger).

## What I'm leaving out, and why

- **Login and roles:** there's only one type of user, so it would add work without showing anything about the actual problem. If I continued, I'd add Spring Security with SSO next.
- **Payroll, tax and payslips:** this is a different problem with country-specific rules. This app is about salary data, not paying people.
- **Bonuses, equity, benefits and reviews:** I'm only covering base salary. Total compensation would need its own data model.
- **Approvals and notifications:** not needed for a single-user tool.
- **Org chart and manager hierarchy:** that's HR system territory and goes beyond salaries.
- **Multiple organisations and other languages:** one company, English only.
- **Excel import screen:** moving off Excel is a one-time job, so a script or simple CSV import is enough if I need it.

## Assumptions

- Salary means annual base pay.
- One HR user at a time. If two edits collide, the last save wins.
- At around 10,000 employees, plain SQL with indexes is enough. No caching or search engine needed.

## How I'll know it works

- The employee list loads in under 300 ms with filters applied.
- Analytics are calculated in the database with GROUP BY and respond in under 500 ms.
- Salaries are stored as `BigDecimal`, never floating point.
- The whole test suite runs in a few seconds without any outside services.
- A fresh clone starts with one command and comes up with the seeded data.

## Tech choices

- **Backend:** Java 17 and Spring Boot with Spring Data JPA and Flyway. It's a familiar, well-structured setup that's easy to test.
- **Database:** H2 in file mode. No setup needed, works smoothly with Hibernate, and handles 10,000 rows easily.
- **Frontend:** Angular with Angular Material.