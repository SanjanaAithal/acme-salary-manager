ALTER TABLE employees ADD COLUMN currency VARCHAR(3) DEFAULT 'USD' NOT NULL;
ALTER TABLE employees ADD COLUMN salary_usd NUMERIC(15,2);

UPDATE employees SET salary_usd = salary;

ALTER TABLE employees ALTER COLUMN salary_usd SET NOT NULL;
ALTER TABLE employees ALTER COLUMN currency DROP DEFAULT;