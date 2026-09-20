package org.example.acmesalarymanager.employee;

import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class EmployeeSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> matching(EmployeeFilter filter) {
        return Specification.<Employee>unrestricted()
                .and(equalTo("country", filter.country()))
                .and(equalTo("department", filter.department()))
                .and(equalTo("jobTitle", filter.jobTitle()))
                .and(hasStatus(filter.status()))
                .and(nameOrEmailContains(filter.search()));
    }

    private static Specification<Employee> equalTo(String field, String value) {
        if (isBlank(value)) {
            return Specification.unrestricted();
        }
        String trimmed = value.trim();
        return (root, query, cb) -> cb.equal(root.get(field), trimmed);
    }

    private static Specification<Employee> hasStatus(EmploymentStatus status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Employee> nameOrEmailContains(String term) {
        if (isBlank(term)) {
            return Specification.unrestricted();
        }
        String pattern = "%" + escapeLike(term.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("fullName")), pattern, LIKE_ESCAPE),
                cb.like(cb.lower(root.get("email")), pattern, LIKE_ESCAPE));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}