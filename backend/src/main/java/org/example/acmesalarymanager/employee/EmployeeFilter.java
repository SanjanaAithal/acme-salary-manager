package org.example.acmesalarymanager.employee;

public record EmployeeFilter(
        String search,
        String country,
        String department,
        String jobTitle,
        EmploymentStatus status
) {

    public static EmployeeFilter empty() {
        return new EmployeeFilter(null, null, null, null, null);
    }
}