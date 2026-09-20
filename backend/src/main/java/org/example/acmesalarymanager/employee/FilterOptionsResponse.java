package org.example.acmesalarymanager.employee;

import java.util.List;

public record FilterOptionsResponse(
        List<String> countries,
        List<String> departments,
        List<String> jobTitles,
        List<EmploymentStatus> statuses
) {
}